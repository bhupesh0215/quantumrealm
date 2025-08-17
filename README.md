# Color Cascade - Android Puzzle Game

A hyper-casual puzzle game where players drop colored blocks that merge when matching colors touch, creating satisfying chain reactions.

## 🎮 Game Features

- **Simple Controls**: Swipe left/right to move blocks, tap to drop
- **Color Matching**: Match 3+ blocks of the same color to merge them
- **Chain Reactions**: Merged blocks create cascading effects
- **Progressive Difficulty**: Game gets more challenging as you progress
- **Beautiful Graphics**: Smooth animations and polished visual effects

## 🎯 How to Play

1. **Move**: Swipe left or right to move the falling block
2. **Drop**: Tap the screen to drop the block quickly
3. **Match**: Connect 3 or more blocks of the same color
4. **Score**: Earn points for successful merges and chain reactions
5. **Survive**: Keep playing until blocks reach the top

## 🏗️ Project Structure

```
app/
├── src/main/java/com/colorcascade/game/
│   ├── MainActivity.kt       # Main activity
│   ├── GameView.kt          # Game rendering and input handling
│   ├── GameEngine.kt        # Core game logic
│   └── Block.kt             # Block data class
├── src/main/res/
│   ├── values/
│   │   ├── strings.xml      # App strings
│   │   ├── colors.xml       # Color definitions
│   │   └── themes.xml       # App themes
│   └── xml/                 # Backup and data extraction rules
└── AndroidManifest.xml      # App configuration
```

## 🛠️ Technical Details

- **Language**: Kotlin
- **UI Framework**: Custom Canvas drawing with SurfaceView
- **Architecture**: Game loop with separated rendering and logic
- **Target SDK**: 34 (Android 14)
- **Minimum SDK**: 24 (Android 7.0)

## 🚀 Building and Running

### Prerequisites
- Android Studio Arctic Fox or newer
- Android SDK 34
- Kotlin 1.9.22+

### Setup
1. Open the project in Android Studio
2. Sync project with Gradle files
3. Connect an Android device or start an emulator
4. Run the app

### Build Commands
```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Install and run
./gradlew installDebug
```

## 🎨 Game Mechanics

### Block Physics
- Blocks fall with gravity simulation
- Collision detection with boundaries and other blocks
- Smooth movement and positioning

### Merge System
- Detects connected blocks of the same color
- Merges groups of 3+ blocks into larger blocks
- Calculates chain reactions and bonus scoring

### Scoring
- Base points for each merge
- Bonus points for larger merges
- Chain reaction multipliers

## 🔧 Customization

### Adding New Colors
Edit `Block.kt` to add new colors to the `colors` array:
```kotlin
private val colors = arrayOf(
    Color.parseColor("#FF6B6B"), // Red
    Color.parseColor("#YOUR_COLOR"), // Your new color
    // ... more colors
)
```

### Adjusting Difficulty
Modify these values in `GameEngine.kt`:
- `gravity`: Speed of block falling
- `dropSpeed`: Speed when manually dropping
- `gridWidth/gridHeight`: Game area size

### Visual Customization
Update colors and themes in:
- `app/src/main/res/values/colors.xml`
- `app/src/main/res/values/themes.xml`

## 📱 Monetization Ideas

- **Ads**: Interstitial ads between games
- **In-App Purchases**: 
  - Remove ads
  - Special block skins
  - Power-ups (bomb blocks, color changers)
  - Extra lives or continues

## 🔄 Future Enhancements

- [ ] Sound effects and background music
- [ ] Particle effects for merges
- [ ] Power-ups and special blocks
- [ ] Local high scores and achievements
- [ ] Online leaderboards
- [ ] Multiple game modes
- [ ] Theme customization
- [ ] Haptic feedback

## 📄 License

This project is open source and available under the MIT License.

## 🤝 Contributing

Feel free to submit issues, feature requests, and pull requests to improve the game!
