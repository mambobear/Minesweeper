package minesweeper;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Random;

import static minesweeper.CellStatus.*;

public class MineField {

    private final int SIZE = 9;
    private final Cell[][] field = new Cell[SIZE][SIZE];
    private int nMines;

    private boolean firstMove = true;

    private int nUnexploredCells = 0;

    MineField(String fileName) {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            char ch;
            this.nMines = 0;
            for (int row = 0; row < SIZE; row++) {
                char[] rowChars = reader.readLine().toCharArray();
                for (int col = 0; col < SIZE; col++) {
                    ch = rowChars[col];
                    if (ch == '.') {
                        field[row][col] = new EmptyCell(row, col);
                    } else if (ch == 'X') {
                        field[row][col] = new MineCell(row, col);
                        this.nMines++;
                    }
                }
            }
            this.nUnexploredCells = SIZE * SIZE - this.nMines;
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    MineField(int nMines) {
        this.nMines = nMines;
        this.nUnexploredCells = SIZE * SIZE - nMines;

        generateField();
    }

    private void generateField() {
        int N = this.SIZE * this.SIZE;
        Random rand = new Random(System.currentTimeMillis());
        int start = 0;
        int row;
        int col;
        int nMines = this.nMines;
        while (nMines > 0) {
            int idx = rand.nextInt(N - start - nMines + 1) + start;
            for (int i = start; i < idx; i++) {
                row = i / this.SIZE;
                col = i % this.SIZE;
                this.field[row][col] = new EmptyCell(row, col);
            }
            row = idx / this.SIZE;
            col = idx % this.SIZE;
            this.field[row][col] = new MineCell(row, col);
            start = idx + 1;
            nMines--;
        }
        for (int i = start; i < N; i++) {
            row = i / this.SIZE;
            col = i % this.SIZE;
            this.field[row][col] = new EmptyCell(row, col);
        }
    }

    private void computeNeighbouringMines() {
        for (int row = 0; row < this.SIZE; row++) {
            for (int col = 0; col < this.SIZE; col++) {
                if (this.field[row][col] instanceof EmptyCell) {
                    ((EmptyCell) this.field[row][col]).setNeighbourMineCount(numberOfMineNeighbors(row, col));
                }
            }
        }
    }

    private short numberOfMineNeighbors(int row, int col) {
        short count = 0;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int neighborRow = row + dr;
                int neighborCol = col + dc;
                if (isValid(neighborRow, neighborCol) && (this.field[neighborRow][neighborCol] instanceof MineCell)) {
                    count++;
                }
            }
        }
        return count;
    }

    public Status markPosition(Coordinates coords, CommandType commandType) {
        int row = coords.getRow();
        int col = coords.getCol();

        Cell cell = this.field[row][col];

        if (firstMove) {
            firstMove = false;
            if (cell instanceof MineCell) {
                adjustField(row, col);
            }
            computeNeighbouringMines();
        }

        cell = this.field[row][col];
        if (commandType == CommandType.FREE) {
            if (cell instanceof EmptyCell) {
                if (cell.status == CellStatus.NONE) {
                    markFreeCells(cell);
                    if (this.nUnexploredCells == 0) return Status.WIN;
                    else return Status.IN_PROGRESS;
                }
            } else { // CellType.MINE
                return Status.LOSS;
            }
        } else { // CommandType.MINE
            if (cell.status == MARKED) {
                cell.status = NONE;
                if (cell instanceof MineCell) this.nMines++;
            } else if (cell.status == NONE) {
                cell.status = MARKED;
                if (cell instanceof MineCell) this.nMines--;
            }
            if (this.nMines == 0) return Status.WIN;
            else return Status.IN_PROGRESS;
        }

        return Status.IN_PROGRESS;
    }

    private void adjustField(int row, int col) {

        assert (this.field[row][col] instanceof MineCell);

        // find random Empty cell to switch with cell at row, col
        Random rand = new Random(System.currentTimeMillis());
        int idx = rand.nextInt(this.SIZE * this.SIZE - this.nMines) + 1;
        int count = 0;
        for (int r = 0; r < this.SIZE; r++) {
            for (int c = 0; c < this.SIZE; c++) {
                if (this.field[r][c] instanceof EmptyCell) count++;
                if (count == idx) {
                    this.field[r][c] = new MineCell(r, c);
                    this.field[row][col] = new EmptyCell(row, col);
                }
            }
        }
    }

    private void markFreeCells(Cell cell) {
        assert cell instanceof EmptyCell;
        ArrayDeque<Cell> cells = new ArrayDeque<>();
        cell.status = EXPLORED;
        this.nUnexploredCells--;
        cells.add(cell);

        while (!cells.isEmpty()) {
            EmptyCell nextCell = (EmptyCell) cells.poll();
            if (nextCell.getNeighbourMineCount() == 0) {
                for (int dr = -1; dr <= 1; dr++) {
                    for (int dc = -1; dc <= 1; dc++) {
                        if (dr == 0 && dc == 0) continue;
                        int neighborRow = nextCell.row + dr;
                        int neighborCol = nextCell.col + dc;
                        if (isValid(neighborRow, neighborCol)) {
                            Cell neighbor = cellAt(neighborRow, neighborCol);
                            if (neighbor.status == EXPLORED) continue;
                            if (neighbor.status == NONE || (neighbor instanceof EmptyCell && neighbor.status == MARKED)) {
                                neighbor.status = EXPLORED;
                                this.nUnexploredCells--;
                                cells.add(neighbor);
                            }
                        }
                    }
                }
            }
        }
    }

    private Cell cellAt(int row, int col) {
        return this.field[row][col];
    }

    private boolean isValid(int neighborRow, int neighborCol) {
        return (neighborRow >= 0 && neighborRow < this.SIZE && neighborCol >= 0 && neighborCol < this.SIZE);
    }

    public void print(boolean withMines) {
        StringBuilder strb = new StringBuilder(" |123456789|\n");
        strb.append("—│—————————│\n");
        for (int i = 0; i < this.SIZE; i++) {
            Cell[] row = this.field[i];
            strb.append(i + 1).append("|");
            for (Cell cell : row) {
                if (withMines && (cell instanceof MineCell)) strb.append('X');
                else strb.append(cell.symbol());
            }
            strb.append("|\n");
        }
        strb.append("—│—————————│\n");
        System.out.println(strb);
    }

    public void printDebug() {
        StringBuilder strb = new StringBuilder();

        for (Cell[] row : field) {
            for (Cell cell : row) {
                if (cell instanceof EmptyCell) {
                    EmptyCell emptyCell = (EmptyCell) cell;
                    if (emptyCell.getNeighbourMineCount() == 0) {
                        strb.append('.');
                    } else {
                        strb.append(Character.forDigit(emptyCell.getNeighbourMineCount(), 10));
                    }
                } else if (cell instanceof MineCell) strb.append('X');
            }
            strb.append("\n");
        }
        System.out.println("DEBUG:\n" + strb + "\n");
    }
}

class Command {

    private final Coordinates coords;
    private final CommandType commandType;

    Command(Coordinates coords, String commandTypeStr) {
        this.coords = coords;
        if ("free".equals(commandTypeStr)) this.commandType = CommandType.FREE;
        else if ("mine".equals(commandTypeStr)) this.commandType = CommandType.MINE;
        else {
            throw new IllegalArgumentException("Illegal command" + commandTypeStr);
        }
    }

    public Coordinates getCoords() {
        return coords;
    }

    public CommandType getCommandType() {
        return commandType;
    }
}

class Coordinates {
    int row;
    int col;

    Coordinates(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return this.row;
    }

    public int getCol() {
        return this.col;
    }
}
