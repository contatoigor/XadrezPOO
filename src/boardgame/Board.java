package boardgame;

import java.io.Serial;
import java.io.Serializable;

public class Board implements Serializable{
	@Serial
	private static final long serialVersionUID = -2745932311301004228L;

	private final int rows;
	private final int columns;
	private Piece[][] pieces;

	public Board(int rows, int columns) {
		if (rows < 1 || columns < 1)
			throw new BoardException("Error creating board: there must be at least 1 row and 1 column");
		this.rows = rows;
		this.columns = columns;
		this.pieces = new Piece[rows][columns];
	}

	public int getRows() {
		return rows;
	}

	public int getColumns() {
		return columns;
	}

	public Piece getPiece(int row, int column) {
		if (!positionExists(row, column))
			throw new BoardException("Position are not on the board");
		return pieces[row][column];
	}

	public Piece getPiece(Position position) {
		if (!positionExists(position))
			throw new BoardException("Position are not on the board");
		return pieces[position.getRow()][position.getColumn()];
	}

	public void placePiece(Piece piece, Position position) {
		if (thereIsAPiece(position))
			throw new BoardException("There is already a piece on position " + position);
		this.pieces[position.getRow()][position.getColumn()] = piece;
		piece.position = position;
	}

	public Piece removePiece(Position position) {
		if (!positionExists(position))
			throw new BoardException("Position are not on the board");

		Piece a = getPiece(position);
		if (a == null)
			return null;
		a.position = null;
		pieces[position.getRow()][position.getColumn()] = null;
		return a;
	}

	private boolean positionExists(int row, int column) {
		return row >= 0 && row < this.rows && column >= 0 && column < this.columns;
	}

	public boolean positionExists(Position position) {
		return positionExists(position.getRow(), position.getColumn());
	}

	public boolean thereIsAPiece(Position position) {
		if (!positionExists(position))
			throw new BoardException("Position are not on the board");
		return getPiece(position) != null;
	}
}
