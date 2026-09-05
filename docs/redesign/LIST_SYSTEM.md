# The list system

Almost every screen in Kotatsu is a list of manga. Four layouts draw one, and before this pass each
had drifted into its own spacing, type and corner radius. This file records the rules they now share,
so a fifth one does not start the drift again.

Covered by screenshot baselines in `app/src/test/screenshots`: `manga-row-compact`,
`manga-row-detailed`, `manga-grid-cell`, `manga-rail-cell`, `list-header`.

## The four ways a manga is drawn

| Layout | Used for | Cover |
| --- | --- | --- |
| `item_manga_list` | compact rows: search results, feeds | 40dp square thumbnail |
| `item_manga_list_details` | detailed rows: title, author, tags | 118dp tall, 2:3 |
| `item_manga_grid` | the default grid | column width, ratio from the cover style |
| `item_manga_rail_cell` | home rails | `preferred_grid_width`, ratio from the cover style |

## Rules

**Type.** The manga's title is `?attr/textAppearanceTitleSmall` in all four. Anything secondary —
subtitle, tags — is `?attr/textAppearanceBodySmall` with `?attr/colorOnSurfaceVariant`. The detailed
row keeps `BodyMedium` for the author, which sits between the two on purpose.

A section header is one step above a row title, through `TextAppearance.Kotatsu.SectionHeader`.
Headers and rows used to share `TitleSmall`, which left a date group looking like just another manga.

That header size is deliberately **not** a theme attribute. It was `?attr/textAppearanceTitleMedium`
at first, with the attribute itself inflated from Material's 16sp to 18sp — which quietly enlarged
every androidx preference row in the app, because settings titles resolve the same attribute. Theme
attributes are shared by things you are not looking at; reach for one only when you mean to change
all of them.

Do not set `app:lineHeight` on a list item. The type scale in `values/type.xml` already defines line
height per style; the two hard-coded values that used to be here (18dp under a 16sp face, 14dp under
13sp) fought it and made the detailed row cramped.

**Spacing** comes from the tokens in `values/tokens.xml` — `space_1` through `space_8`. No raw `dp`
in a list item layout.

**Corner radius** scales with the cover: `ShapeAppearanceOverlay.Kotatsu.Cover` (12dp) for grid
cells, `.Cover.Medium` (8dp) for the mid-size covers in detailed rows and rail cells, `.Cover.Small`
(4dp) for the 40dp thumbnail. All three now reference `radius_*` tokens rather than literals.

**Cover overlays** — the unread badge, the reading-progress ring — are inset `space_2` from their
corner, so the two read as a pair on the same cover.

**Press state** is `@drawable/custom_selectable_item_background`: a ripple in `@color/selector_overlay`,
rounded to `list_selector_corner`. `list_selector` is the same ripple plus `state_selected`,
`state_activated` and `state_hovered` fills, and is used where those states can occur. Nothing in the
app sets `isActivated` today — multi-select is drawn by `AbstractSelectionItemDecoration`, not by the
item background — but `state_hovered` still fires for a mouse or stylus.

## Where the rules live

`values/styles_list.xml`. A layout says where a part goes; the style says what it looks like.

| Style | Applies to |
| --- | --- |
| `Widget.Kotatsu.MangaCover` (`.Medium`, `.Thumbnail`) | the cover, including its corner radius |
| `Widget.Kotatsu.MangaBadge` | unread count |
| `Widget.Kotatsu.MangaProgress` | reading progress ring |
| `Widget.Kotatsu.MangaOverlayIcons` | state icons over the cover |
| `Widget.Kotatsu.ListItem.Title` / `.Subtitle` | the two text roles |
| `Widget.Kotatsu.SectionHeader` | the heading above a section, in a list or on the details screen |
| `Widget.Kotatsu.FieldLabel` | a field name in the details table |

Positioning stays in the layout on purpose: constraints and gravity belong to the parent, and a
shared style cannot know whether it is being applied inside a `ConstraintLayout` or a `FrameLayout`.
Line counts stay in the layout too, since they are what actually differ between a compact row and a
detailed one.

Five layouts use these: the four above plus `item_manga_alternative`, which had drifted furthest —
a 10dp icon inset, 4dp padding and its own copy of every cover attribute.

The last two styles reach past lists. The details screen wrote out its own section headings and its
own field labels, and once list headers moved to `TitleMedium` those copies were left behind at
`TitleSmall` — the drift starting again the same week it was documented. Details now uses the same
two styles, so "Description" on that screen and a date group in history are the same thing said
once. The table's field names moved off `TitleSmall` in the process: a label is not a heading, and
calling it one made every row shout.

Styles were the tool rather than `<include>`, which would have been the other way to share this.
An `<include>` changes ViewBinding paths (`binding.imageViewCover` becomes
`binding.cover.imageViewCover`), so every adapter that binds a manga would have had to change to
share markup that is only a handful of attributes. The styles get the same single source of truth
without touching a single binding.

## Two traps

**Margins are not the whole story.** `TypedListSpacingDecoration` adds `list_spacing_normal`
horizontally to most item types on top of whatever the layout declares. The compact row's 8dp start
margin is 16dp on screen because of it. Change one without checking the other and the result is
double-shifted.

**A grid that looks broken may be correct.** Grouped lists — history, the feed — render each date as
its own section, and a group with fewer items than the span count leaves the rest of the row empty.
That is the grid working. `GridSpanResolver` never returns fewer than 2 columns, so a genuine
single-column grid would be a different bug entirely.

## Deliberately not changed

The compact row's cover is a 40dp **square** crop of portrait artwork, which loses the shape of the
cover. Making it 2:3 would add roughly 16dp to every row in the densest list mode the app offers,
which is the mode a reader picks precisely for density. Left alone as a deliberate trade, not an
oversight; revisit it with the list-mode settings rather than on its own.
