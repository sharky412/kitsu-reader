#!/usr/bin/env python3
"""Seed a connected emulator with sample library data.

Screens that only render existing reading history — home, history, updates — show nothing at all on
a fresh install, which makes them impossible to review on a device. This writes a small, fixed set
of manga, history entries, favourites and tracked updates straight into the app database, along with
generated cover files the app can read locally, so those screens can be exercised without a network
or a real source.

The data is deliberately synthetic and the timestamps are fixed, so repeated runs produce the same
screen. Requires a rooted emulator image (`adb root`); it will not work on a physical device or on a
Play-flavoured system image.

Usage:  python3 tools/dev/seed-device.py [--package org.koitharu.kotatsu.debug] [--clear]
"""

import argparse
import os
import shutil
import struct
import subprocess
import sys
import tempfile
import zlib

NOW = 1787000000000  # fixed epoch millis, so a re-run reproduces the same screen
DAY = 86400000
HOUR = 3600000
SOURCE = "MANGADEX"
COVER_W, COVER_H, COVER_PAD = 240, 360, 10

HISTORY = [
    ("Frieren: Beyond Journey's End", "Kanehito Yamada", ["Adventure", "Fantasy"], 0.72),
    ("Vinland Saga", "Makoto Yukimura", ["Action", "Historical"], 0.35),
    ("Blue Period", "Tsubasa Yamaguchi", ["Drama", "School Life"], 0.91),
    ("Witch Hat Atelier", "Kamome Shirahama", ["Fantasy", "Adventure"], 0.14),
    ("Oyasumi Punpun", "Inio Asano", ["Drama", "Psychological"], 0.58),
    ("Dungeon Meshi", "Ryoko Kui", ["Comedy", "Fantasy"], 0.05),
]
TRACKED = [
    ("One Piece", "Eiichiro Oda", ["Action", "Adventure"], 3),
    ("Jujutsu Kaisen", "Gege Akutami", ["Action", "Supernatural"], 1),
    ("Chainsaw Man", "Tatsuki Fujimoto", ["Action", "Horror"], 12),
    ("Kaiju No. 8", "Naoya Matsumoto", ["Action", "Sci-Fi"], 2),
    ("Spy x Family", "Tatsuya Endo", ["Comedy", "Slice of Life"], 5),
    ("Sakamoto Days", "Yuto Suzuki", ["Action", "Comedy"], 7),
]

PALETTE = [
    (0xE8, 0x4A, 0x5F), (0x35, 0x5C, 0x7D), (0x6C, 0x56, 0x7B), (0xC0, 0x6C, 0x84),
    (0x2A, 0x36, 0x3B), (0x01, 0x9F, 0xB7), (0xEC, 0x82, 0x2A), (0x4A, 0x7C, 0x59),
    (0x8E, 0x44, 0xAD), (0x16, 0xA0, 0x85), (0xD3, 0x54, 0x00), (0x2C, 0x3E, 0x50),
]


def adb(*args, check=True):
    result = subprocess.run(["adb", *args], capture_output=True, text=True)
    if check and result.returncode != 0:
        detail = (result.stderr or result.stdout).strip()
        sys.exit(f"adb {' '.join(args)} failed: {detail}")
    return result


def ensure_database(package, database):
    """Makes sure the app has created its database.

    A fresh install, or one just cleared with `pm clear`, has no database file until the app runs
    once. Start it, wait for the file to appear, then stop it again so the seed writes to a database
    nobody has open.
    """
    if adb("shell", f"test -f {database} && echo yes", check=False).stdout.strip() == "yes":
        return
    print("no database yet, starting the app once to create it")
    adb("shell", "am", "start", "-n", f"{package}/org.koitharu.kotatsu.main.ui.MainActivity")
    for _ in range(30):
        subprocess.run(["sleep", "1"], check=False)
        if adb("shell", f"test -f {database} && echo yes", check=False).stdout.strip() == "yes":
            adb("shell", "am", "force-stop", package)
            return
    sys.exit("the app did not create its database; start it manually and try again")


def write_png(path, width, height, color_at):
    raw = bytearray()
    for y in range(height):
        raw.append(0)  # no per-scanline filter
        for x in range(width):
            raw.extend(color_at(x, y))

    def chunk(kind, data):
        body = kind + data
        return struct.pack(">I", len(data)) + body + struct.pack(">I", zlib.crc32(body) & 0xFFFFFFFF)

    with open(path, "wb") as out:
        out.write(b"\x89PNG\r\n\x1a\n")
        out.write(chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, 2, 0, 0, 0)))
        out.write(chunk(b"IDAT", zlib.compress(bytes(raw), 9)))
        out.write(chunk(b"IEND", b""))


def generate_covers(directory):
    """Draws covers that vary along both axes.

    `TrimTransformation` crops uniform edges off a cover, so an image whose rows or columns are each
    a single colour is degenerate input. The blocks below keep the artwork non-uniform, and the plain
    border gives the transformation something real to remove.
    """
    for index, base in enumerate(PALETTE, start=1):
        def color_at(x, y, base=base, index=index):
            if x < COVER_PAD or x >= COVER_W - COVER_PAD or y < COVER_PAD or y >= COVER_H - COVER_PAD:
                return bytes((0xF2, 0xF2, 0xF2))
            fade = (y - COVER_PAD) / (COVER_H - 2 * COVER_PAD)
            r, g, b = (int(c * (0.5 + 0.5 * (1 - fade))) for c in base)
            block_x, block_y = (x - COVER_PAD) // 30, (y - COVER_PAD) // 30
            if (block_x * 7 + block_y * 13 + index * 3) % 5 == 0:
                r, g, b = 255 - r, 255 - g, 255 - b
            if 120 <= y - COVER_PAD <= 200 and 20 <= x - COVER_PAD <= COVER_W - 2 * COVER_PAD - 20:
                r, g, b = (0xF8, 0xF8, 0xF8) if (x // 8 + y // 8) % 2 else (0x18, 0x18, 0x18)
            return bytes((r, g, b))

        write_png(os.path.join(directory, f"cover_{index:02d}.png"), COVER_W, COVER_H, color_at)


def build_local_manga(path, chapters=3, pages=6):
    """Writes a CBZ the app can index without a source.

    `LocalMangaParser` reads a zip whose root holds one directory per chapter, each holding page
    images. No index.json is needed; the titles come from the file and directory names. This gives
    the details screen real chapters and the reader real pages on a device with no network.
    """
    import zipfile

    with zipfile.ZipFile(path, "w", zipfile.ZIP_DEFLATED) as archive:
        for chapter in range(1, chapters + 1):
            for page in range(1, pages + 1):
                hue = (chapter * 60 + page * 12) % 360
                image = os.path.join(os.path.dirname(path), "page.png")
                write_png(image, 720, 1080, page_painter(hue, chapter, page))
                archive.write(image, f"Chapter {chapter}/Page {page:02d}.png")
        os.remove(os.path.join(os.path.dirname(path), "page.png"))


def page_painter(hue, chapter, page):
    """A page that is obviously page N of chapter M without needing a font."""
    base = hsv_to_rgb(hue)

    def paint(x, y):
        # A band of blocks across the middle counts the page; the bar down the side counts chapters.
        if 40 <= x < 40 + chapter * 60 and 40 <= y < 100:
            return bytes((0x11, 0x11, 0x11))
        if 300 <= y < 420 and 60 <= x < 60 + page * 90:
            return bytes((0xF5, 0xF5, 0xF5))
        if x < 24 or x >= 720 - 24 or y < 24 or y >= 1080 - 24:
            return bytes((0x22, 0x22, 0x22))
        shade = 0.7 + 0.3 * (y / 1080)
        return bytes(int(c * shade) for c in base)

    return paint


def hsv_to_rgb(hue):
    import colorsys

    r, g, b = colorsys.hsv_to_rgb(hue / 360.0, 0.45, 0.85)
    return int(r * 255), int(g * 255), int(b * 255)


def quote(value):
    return "'" + value.replace("'", "''") + "'"


def chapter_names(count):
    """Chapter names for one feed entry, newline separated the way the tracker writes them."""
    return "\n".join(f"Chapter {100 + n}" for n in range(count))


def build_sql(covers_dir):
    tag_ids = {}

    def tag_id(name):
        return tag_ids.setdefault(name, 9000 + len(tag_ids))

    manga_rows, tag_links = [], []

    def add_manga(manga_id, title, author, tags, cover_index):
        cover = f"file://{covers_dir}/cover_{cover_index:02d}.png"
        slug = title.lower().replace(" ", "-").replace(":", "").replace(".", "")
        manga_rows.append(
            f"({manga_id}, {quote(title)}, NULL, {quote('/title/' + slug)}, "
            f"{quote('https://mangadex.org/title/' + slug)}, 0.8, 0, 'SAFE', {quote(cover)}, "
            f"{quote(cover)}, 'ONGOING', {quote(author)}, {quote(SOURCE)})"
        )
        tag_links.extend(f"({manga_id}, {tag_id(name)})" for name in tags)

    for i, (title, author, tags, _) in enumerate(HISTORY):
        add_manga(1001 + i, title, author, tags, i + 1)
    for i, (title, author, tags, _) in enumerate(TRACKED):
        add_manga(2001 + i, title, author, tags, i + 7)

    statements = [
        "PRAGMA foreign_keys=ON;",
        "BEGIN;",
        "INSERT OR REPLACE INTO manga (manga_id, title, alt_title, url, public_url, rating, nsfw, "
        "content_rating, cover_url, large_cover_url, state, author, source) VALUES\n  "
        + ",\n  ".join(manga_rows) + ";",
        "INSERT OR REPLACE INTO tags (tag_id, title, key, source, pinned) VALUES\n  "
        + ",\n  ".join(
            f"({tid}, {quote(name)}, {quote(name.lower().replace(' ', '_'))}, {quote(SOURCE)}, 0)"
            for name, tid in tag_ids.items()
        ) + ";",
        "INSERT OR REPLACE INTO manga_tags (manga_id, tag_id) VALUES\n  " + ",\n  ".join(tag_links) + ";",
        # Two entries per day rather than one: history groups by date, and one item per group makes
        # a multi-column grid look like it failed to lay out when it is doing exactly what it should.
        "INSERT OR REPLACE INTO history (manga_id, created_at, updated_at, chapter_id, page, scroll, "
        "percent, deleted_at, chapters) VALUES\n  "
        + ",\n  ".join(
            f"({1001 + i}, {NOW - (i // 2) * DAY - 30 * DAY}, {NOW - (i // 2) * DAY - (i % 2) * HOUR}, "
            f"{5000 + i}, 12, 0.0, {pct}, 0, 40)"
            for i, (_, _, _, pct) in enumerate(HISTORY)
        ) + ";",
        # Tracks are garbage collected unless their manga sits in history or in a tracked category.
        "INSERT OR REPLACE INTO favourite_categories (category_id, created_at, sort_key, title, "
        f"\"order\", track, show_in_lib, deleted_at) VALUES (1, {NOW}, 0, 'Reading', 'NEWEST', 1, 1, 0);",
        "INSERT OR REPLACE INTO favourites (manga_id, category_id, sort_key, pinned, created_at, "
        "deleted_at) VALUES\n  "
        + ",\n  ".join(f"({2001 + i}, 1, {i}, 0, {NOW}, 0)" for i in range(len(TRACKED))) + ";",
        "INSERT OR REPLACE INTO tracks (manga_id, last_chapter_id, chapters_new, last_check_time, "
        "last_chapter_date, last_result, last_error) VALUES\n  "
        + ",\n  ".join(
            f"({2001 + i}, {6000 + i}, {new}, {NOW}, {NOW - i * DAY}, 0, NULL)"
            for i, (_, _, _, new) in enumerate(TRACKED)
        ) + ";",
        # The feed lists individual chapters as they arrived, which is a separate table from the
        # per-manga unread counts above. Without it the feed renders empty next to a populated
        # new-chapters list, which looks like a bug and is not one.
        "DELETE FROM track_logs WHERE manga_id >= 2001;",
        "INSERT INTO track_logs (manga_id, chapters, created_at, unread) VALUES\n  "
        + ",\n  ".join(
            f"({2001 + i}, {quote(chapter_names(new))}, {NOW - i * DAY}, 1)"
            for i, (_, _, _, new) in enumerate(TRACKED)
        ) + ";",
        "INSERT OR REPLACE INTO sources (source, enabled, sort_key, added_in, used_at, pinned, "
        f"cf_state) VALUES ({quote(SOURCE)}, 1, 0, 0, {NOW}, 0, 0);",
        "COMMIT;",
    ]
    return "\n".join(statements) + "\n"


def clear_sql():
    ids = [str(1001 + i) for i in range(len(HISTORY))] + [str(2001 + i) for i in range(len(TRACKED))]
    joined = ", ".join(ids)
    return (
        "PRAGMA foreign_keys=ON;\nBEGIN;\n"
        f"DELETE FROM track_logs WHERE manga_id IN ({joined});\n"
        f"DELETE FROM manga WHERE manga_id IN ({joined});\n"
        "DELETE FROM tags WHERE tag_id >= 9000;\n"
        "DELETE FROM favourite_categories WHERE category_id = 1;\n"
        "COMMIT;\n"
    )


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--package", default="org.koitharu.kotatsu.debug")
    parser.add_argument("--clear", action="store_true", help="remove the seeded rows instead")
    parser.add_argument(
        "--local-manga",
        action="store_true",
        help="also install a generated CBZ so the app has a manga it can actually open",
    )
    options = parser.parse_args()

    package = options.package
    data_dir = f"/data/data/{package}"
    covers_dir = f"{data_dir}/files/covers"
    database = f"{data_dir}/databases/kotatsu-db"

    if not shutil.which("adb"):
        sys.exit("adb is not on PATH")
    if not adb("shell", "pm", "path", package, check=False).stdout.strip():
        sys.exit(f"{package} is not installed on the connected device")

    adb("root")
    adb("wait-for-device")
    adb("shell", "am", "force-stop", package)
    ensure_database(package, database)
    adb("shell", "am", "force-stop", package)

    sql = clear_sql() if options.clear else build_sql(covers_dir)
    with tempfile.TemporaryDirectory() as workspace:
        sql_path = os.path.join(workspace, "seed.sql")
        with open(sql_path, "w") as handle:
            handle.write(sql)
        adb("push", sql_path, "/data/local/tmp/seed.sql")

        if options.clear:
            adb("shell", f"rm -rf {covers_dir}")
        else:
            covers = os.path.join(workspace, "covers")
            os.makedirs(covers)
            generate_covers(covers)
            adb("shell", f"rm -rf /data/local/tmp/covers {covers_dir}")
            adb("push", covers, "/data/local/tmp/covers")
            adb("shell", f"mkdir -p {covers_dir} && cp /data/local/tmp/covers/*.png {covers_dir}/")

        adb("shell", f"sqlite3 {database} < /data/local/tmp/seed.sql")

    # Files written as root end up owned by root with the wrong SELinux label, which the app cannot
    # read. Hand them back before the app is allowed to start again.
    app_uid = adb("shell", f"stat -c %u {data_dir}").stdout.strip()
    adb("shell", f"chown -R {app_uid}:{app_uid} {data_dir}/files {data_dir}/databases")
    adb("shell", f"restorecon -R {data_dir}/files {data_dir}/databases")
    # Covers are cached by URL, so a re-run with regenerated artwork must not serve the old bitmaps.
    adb("shell", f"rm -rf {data_dir}/cache/image_cache", check=False)

    if options.local_manga and not options.clear:
        manga_dir = f"{data_dir}/files/manga"
        with tempfile.TemporaryDirectory() as workspace:
            cbz = os.path.join(workspace, "Sample Manga.cbz")
            build_local_manga(cbz)
            adb("shell", f"mkdir -p {manga_dir}")
            adb("push", cbz, "/data/local/tmp/sample.cbz")
            adb("shell", f"cp /data/local/tmp/sample.cbz '{manga_dir}/Sample Manga.cbz'")
        adb("shell", f"chown -R {app_uid}:{app_uid} {manga_dir}")
        adb("shell", f"restorecon -R {manga_dir}")
        print("installed a local manga: 3 chapters of 6 pages")

    print("cleared" if options.clear else f"seeded {len(HISTORY)} history and {len(TRACKED)} tracked entries")


if __name__ == "__main__":
    main()
