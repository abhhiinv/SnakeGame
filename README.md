# Snake Xenia

A classic Snake game built with Java Swing.

## Features

- **Classic Gameplay** - Control the snake to eat apples and grow longer
- **Difficulty Selection** - Choose Easy, Medium or Hard before each game
- **Score Tracking** - Earn 1 point per apple, displayed in real-time
- **Special Apple** - Spawns every 5 normal apples, worth 5 bonus points, disappears after 10 seconds
- **Special Apple Timer** - Bar at the bottom of the screen shows time remaining
- **High Scores** - Top 5 scores saved to `highscores.txt` with difficulty label
- **Game Over Screen** - Shows your score, leaderboard and whether you set a new high score
- **Collision Detection** - Game ends when hitting walls or yourself
- **Smooth Controls** - Arrow key movement with direction validation (no 180° turns)
- **Retro Graphics** - Custom PNG sprites for snake head, body and apples

## How to Run

```bash
javac SnakeGame.java Board.java MenuPanel.java
java SnakeGame
```