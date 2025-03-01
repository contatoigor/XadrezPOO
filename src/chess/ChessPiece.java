package chess;

import java.io.Serial;
import java.io.Serializable;

import boardgame.Board;
import boardgame.Piece;
import boardgame.Position;

public abstract class ChessPiece extends Piece implements Serializable {
	@Serial
	private static final long serialVersionUID = 692329559904442530L;

	protected Color color;
	protected ChessMatch chessMatch;
	protected int moveCount;

	public ChessPiece(Board board, ChessMatch chessMatch, Color color) {
		super(board);
		this.chessMatch = chessMatch;
		this.color = color;
	}

	public Color getColor() {
		return color;
	}

	public int getMoveCount() {
		return moveCount;
	}

	public void increseMoveCount() {
		moveCount++;
	}

	public void decreseMoveCount() {
		moveCount--;
	}

	// hook method
	@Override
	public boolean[][] getPossibleMoves() {
		boolean[][] pm = getAllPossibleMoves();
		chessMatch.validadePossibleMoves(pm, position);
		return pm;
	}

	public ChessPosition getChessPosition() {
		return ChessPosition.fromPosition(position);
	}

	protected boolean isThereOpponentPiece(Position position) {
		ChessPiece p = (ChessPiece) getBoard().getPiece(position);
		return p != null && !p.getColor().equals(color);
	}

	@Override
	public abstract boolean[][] getAllPossibleMoves();

}
