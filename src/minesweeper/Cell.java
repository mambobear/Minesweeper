package minesweeper;

enum CellStatus {
    EXPLORED, MARKED, NONE
}

abstract class Cell {
    CellStatus status = CellStatus.NONE;
    int row;
    int col;

    abstract char symbol();

    Cell(int row, int col) {
        this.row = row;
        this.col = col;
    }
}

class EmptyCell extends Cell {

    private short neighbourMineCount = 0;

    EmptyCell(int row, int col) {
        super(row, col);
    }

    @Override
    char symbol() {
        char ch = 'E';
        switch (this.status) {
            case NONE:
                ch = '.';
                break;
            case MARKED:
                ch = '*';
                break;
            case EXPLORED:
                if (this.neighbourMineCount == 0) ch = '/';
                else ch = Character.forDigit(this.neighbourMineCount, 10);
                break;
        }
        return ch;
    }

    public short getNeighbourMineCount() {
        return neighbourMineCount;
    }

    public void setNeighbourMineCount(short neighbourMineCount) {
        this.neighbourMineCount = neighbourMineCount;
    }
}

class MineCell extends Cell {

    MineCell(int row, int col) {
        super(row, col);
    }

    @Override
    char symbol() {
        char ch = 'E';
        switch (this.status) {
            case NONE:
                ch = '.';
                break;
            case MARKED:
                ch = '*';
                break;
            case EXPLORED:
                ch = 'X';
                break;
        }
        return ch;
    }
}
