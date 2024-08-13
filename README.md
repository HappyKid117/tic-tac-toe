# Tic Tac Toe  


## Description
A simple game of tic tac toe.


The user will be taken to the sign in screen if they are not logged in. This is checked by a boolean in the sahred view model. It is set to true after signing in. The dashboard fragment will navigate the user to the login fragment if the boolean is set to false.

The user can make the first move because initially turn is set to player 1, which in this case, is the user.

There is an on click listener on the buttons that will ensure the move is valid, if it is, then it checks if it is a winning/tie move.

If the previous move was not a winning move, the computer will make a move (pick the first empty cell). The win checks are run again. If the user wins or loses, appropriate dialogue boxes are shown using the Alert Dialogue feature. The wins and loss counts are updated accordingly.

The game drawn is checked by checking if the game is won by any player, if not, then it checks if the board is full, if it is, then the game is drawn.

Used the firebasee real time database to update every change made by every player and then each player implemented their own listeners to detect changes.

Made the app more accessible by enlgarging the buttons and texts.

Persistence is handled by a view model. Unable to play the game with talkback.
