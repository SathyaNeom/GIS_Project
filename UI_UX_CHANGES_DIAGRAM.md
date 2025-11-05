# MainMapScreen UI/UX Changes - Visual Guide

## 1. Back Button Behavior Flow

```
┌─────────────────────────────────────────────────────────────┐
│                      MainMapScreen                          │
│                                                             │
│  [≡] GPS Device Project                          [🔍]      │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│                                                             │
│                      MAP VIEW                               │
│                                                             │
│                                                      [☰]    │
│                                                      FAB    │
│                                                             │
└─────────────────────────────────────────────────────────────┘

User presses ⬅️ BACK button
          ↓

┌──────────── Decision Tree ────────────┐
│                                       │
│   Is Navigation Drawer Open?          │
│            ↙         ↘                │
│          YES         NO                │
│           ↓           ↓               │
│    Close Drawer   Show Dialog         │
└───────────────────────────────────────┘


When Dialog Shown:
┌─────────────────────────────────────────────────────────────┐
│                   [OVERLAY BACKDROP]                        │
│                                                             │
│        ┌─────────────────────────────────────────┐         │
│        │ ℹ️                                      │         │
│        │                                         │         │
│        │      Logout Confirmation                │         │
│        │                                         │         │
│        │  Are you sure you want to logout?      │         │
│        │  Any unsaved changes will be lost.     │         │
│        │                                         │         │
│        │                    ┌────────┐           │         │
│        │         [Cancel]   │ Logout │           │         │
│        │                    └────────┘           │         │
│        └─────────────────────────────────────────┘         │
│                                                             │
└─────────────────────────────────────────────────────────────┘

Dialog Dismissal Options:
┌────────────────────────────────────────┐
│ 1. Press ⬅️ BACK button                │
│ 2. Tap outside dialog boundaries       │
│ 3. Click "Cancel" button               │
│                                        │
│ Logout Action:                         │
│ - Click "Logout" button                │
│ - Navigates to Login Screen            │
│ - Clears navigation back stack         │
└────────────────────────────────────────┘
```

## 2. Responsive FAB Expansion

### Phone Layout (< 600dp width)

```
COLLAPSED STATE:                      EXPANDED STATE:

┌─────────────────────┐              ┌─────────────────────┐
│                     │              │                [+] │  ← Zoom In
│                     │              │                [−] │  ← Zoom Out
│        MAP          │              │                [⛶] │  ← Fullscreen
│                     │              │                [i] │  ← Identify
│                     │              │                [≣] │  ← Layers
│                     │              │                [×] │  ← Clear
│                     │              │                [⊙] │  ← My Location
│                     │              │                [⚏] │  ← Measure
│                [☰]│ FAB          │                [×]│ ← Main FAB (Close)
└─────────────────────┘              └─────────────────────┘
                                      
       Vertical Expansion
```

### Tablet Layout (≥ 600dp width)

```
COLLAPSED STATE:

┌──────────────────────────────────────────────┐
│                                              │
│                                              │
│                   MAP                     [☰]│ FAB
│                                              │
│                                              │
└──────────────────────────────────────────────┘


EXPANDED STATE:

┌──────────────────────────────────────────────┐
│                                              │
│     [+][−][⛶][i][≣][×][⊙][⚏]      [×]│ FAB
│                                              │
│                   MAP                        │
│                                              │
│                                              │
└──────────────────────────────────────────────┘
        ↑                                      ↑
     Options expand horizontally          Main FAB

Legend:
[+] Zoom In          [≣] Layers
[−] Zoom Out         [×] Clear  
[⛶] Fullscreen       [⊙] My Location
[i] Identify         [⚏] Measure
[×] Close Menu
```

## 3. Component Hierarchy

```
MainMapScreen
│
├── ModalNavigationDrawer
│   └── ESNavigationDrawerContent
│
├── AppScaffold
│   ├── AppTopBar
│   │   ├── Menu Icon (opens drawer)
│   │   └── Search Icon
│   │
│   └── Content
│       ├── MapView (ArcGIS)
│       │
│       ├── FAB + Toolbar (Conditional Layout)
│       │   ├── if (isTablet) → Row Layout
│       │   │   ├── AnimatedVisibility (expandHorizontally)
│       │   │   │   └── Row of MapControlButtons
│       │   │   └── Main FAB
│       │   │
│       │   └── else → Column Layout
│       │       ├── AnimatedVisibility (expandVertically)
│       │       │   └── Column of MapControlButtons
│       │       └── Main FAB
│       │
│       ├── CoordinateInfoBar (bottom)
│       ├── MeasurementModeIndicator (conditional)
│       └── IdentifyModeIndicator (conditional)
│
├── AppDialog (Logout Confirmation)
│   ├── Type: DialogType.INFO
│   ├── Content: Warning message
│   ├── PrimaryButton: "Logout"
│   └── AppTextButton: "Cancel"
│
├── BasemapSelectorDialog (conditional)
├── CollectESBottomSheet (conditional)
├── ManageESBottomSheet (conditional)
└── ProjectSettingsBottomSheet (conditional)
```

## 4. State Management Flow

```
┌─────────────────────────────────────────────────────────────┐
│                    MainMapScreen State                      │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  UI State Variables:                                        │
│  ├── isTablet (computed from LocalConfiguration)           │
│  ├── showLogoutDialog (Boolean)                            │
│  ├── isToolbarExpanded (Boolean)                           │
│  ├── drawerState (DrawerState)                             │
│  └── ... other states                                       │
│                                                             │
│  Event Handlers:                                            │
│  ├── onLogout() → Navigate to Login                        │
│  ├── FAB onClick → Toggle isToolbarExpanded                │
│  └── BackHandler → Show dialog or close drawer             │
│                                                             │
└─────────────────────────────────────────────────────────────┘

State Transitions:

showLogoutDialog: false → true
  Trigger: Back button pressed (when drawer closed)
  Effect: AppDialog appears with overlay

showLogoutDialog: true → false
  Triggers:
  - Back button pressed again
  - Tap outside dialog
  - Click "Cancel"
  - After "Logout" clicked
  Effect: Dialog disappears

isToolbarExpanded: false → true
  Trigger: FAB clicked
  Effect: Map control buttons animate in
          (horizontally on tablet, vertically on phone)

isToolbarExpanded: true → false
  Triggers:
  - FAB clicked again
  - Any map control button clicked
  Effect: Map control buttons animate out
```

## 5. Animation Details

### Vertical Expansion (Phone)

```
Step 1: FAB only           Step 2: Buttons appear     Step 3: All visible
                                   ↓
   [☰]                        [+]                        [+]
                              [−]                        [−]
                              [⛶]                        [⛶]
                           expanding                     [i]
                                                         [≣]
                                                         [×]
                                                         [⊙]
                                                         [⚏]
   [☰]                        [☰]                        [×]

Animation: expandVertically() + fadeIn()
Duration: 300ms (Material Motion default)
Easing: FastOutSlowInEasing
```

### Horizontal Expansion (Tablet)

```
Step 1: FAB only

                                                       [☰]

Step 2: Buttons appear from right

                           [+][−][⛶][i][≣][×][⊙][⚏] expanding [☰]

Step 3: All visible

                           [+][−][⛶][i][≣][×][⊙][⚏]   [×]

Animation: expandHorizontally() + fadeIn()
Duration: 300ms (Material Motion default)
Easing: FastOutSlowInEasing
```

## 6. Design System Integration

```
┌──────────────────────────────────────────────┐
│         Design System Components Used        │
├──────────────────────────────────────────────┤
│                                              │
│  AppDialog                                   │
│  ├── Type: DialogType.INFO                   │
│  ├── Color: InfoBlue (#2196F3)              │
│  └── Icon: Icons.Default.Info                │
│                                              │
│  PrimaryButton (Yellow)                      │
│  └── Text: "Logout"                          │
│                                              │
│  AppTextButton (Text only)                   │
│  └── Text: "Cancel"                          │
│                                              │
│  FloatingActionButton                        │
│  ├── Container: primaryContainer             │
│  └── Content: onPrimaryContainer             │
│                                              │
│  Spacing Constants                           │
│  ├── Spacing.normal (16dp)                   │
│  └── Spacing.small (8dp)                     │
│                                              │
│  Animations                                  │
│  ├── expandVertically / expandHorizontally   │
│  ├── shrinkVertically / shrinkHorizontally   │
│  ├── fadeIn / fadeOut                        │
│  └── Material Motion timing                  │
│                                              │
└──────────────────────────────────────────────┘
```

## 7. Accessibility Features

```
┌─────────────────────────────────────────────┐
│          Accessibility Compliance           │
├─────────────────────────────────────────────┤
│                                             │
│  ✅ Touch Targets                           │
│     All buttons: 48dp × 48dp minimum        │
│                                             │
│  ✅ Content Descriptions                    │
│     - FAB: "Map Controls"                   │
│     - Each button: Descriptive label        │
│     - Dialog icon: Auto-handled             │
│                                             │
│  ✅ Keyboard Navigation                     │
│     - Back button support                   │
│     - Dialog dismissal via back             │
│     - Focus order maintained                │
│                                             │
│  ✅ Color Contrast (WCAG AA)                │
│     - Dialog INFO blue: Sufficient contrast │
│     - Button text: onPrimary color          │
│     - Background overlays: 50% opacity      │
│                                             │
│  ✅ Screen Reader Support                   │
│     - All interactive elements labeled      │
│     - State changes announced               │
│     - Dialog content readable               │
│                                             │
└─────────────────────────────────────────────┘
```

## 8. Responsive Breakpoint

```
Device Classification:
═══════════════════════════════════════════════

0dp                    600dp                   ∞
├───────────────────────┼───────────────────────┤
│       PHONE          │        TABLET         │
│                      │                       │
│   Vertical FAB       │    Horizontal FAB     │
│   expansion          │    expansion          │
│                      │                       │
└──────────────────────┴───────────────────────┘

Detection Logic:
val configuration = LocalConfiguration.current
val isTablet = configuration.screenWidthDp >= 600

Examples:
- Phone (360dp width)  → Vertical layout
- Phone (411dp width)  → Vertical layout
- Tablet (600dp width) → Horizontal layout
- Tablet (768dp width) → Horizontal layout
- Desktop simulation   → Horizontal layout
```

---

**Visual Reference Legend:**

- `[☰]` = Menu/Hamburger Icon
- `[×]` = Close Icon
- `[+]` = Zoom In
- `[−]` = Zoom Out
- `[⛶]` = Fullscreen
- `[i]` = Info/Identify
- `[≣]` = Layers
- `[⊙]` = My Location
- `[⚏]` = Measure
- `[🔍]` = Search
- `[≡]` = Navigation Drawer
- `ℹ️` = Information Dialog Icon
