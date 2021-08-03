# AlphaChess
 A UCI Java Chess Engine inspired by Stockfish.

# Features
 - Bitboard representation
 - Integer representation for encoding moves
 - Fast undo move
 - Stockfish-like classical evaluation
 - Negamax search with alpha-beta optimization
 - Move ordering:
  - History heuristic
  - CaptureHistory heuristic
  - Buttferfly heuristic
  - Countermove heuristic
 - Transposition table (1-256MB)
 - Evalution pruning (Static null move pruning)