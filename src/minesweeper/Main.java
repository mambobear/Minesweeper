package minesweeper;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        MineField mineField;
        Scanner scanner = new Scanner(System.in);

        if (args.length != 0) {
            mineField = new MineField(args[0].strip());
        } else {
            System.out.print("How many mines do you want on the field? ");
            int nMines = Integer.parseInt(scanner.nextLine().strip());
            mineField = new MineField(nMines);
        }

        Status status = Status.IN_PROGRESS;
        while (true) {
            if (status != Status.NUMBER) {
                mineField.print(false);
                //mineField.printDebug();
            }
            Command command = getCommand(scanner);
            status = mineField.markPosition(command.getCoords(), command.getCommandType());
            switch (status) {
                case WIN:
                    mineField.print(false);
                    System.out.println("Congratulations! You found all the mines!");
                    return;
                case LOSS:
                    mineField.print(true);
                    System.out.println("You stepped on a mine and failed!");
                    return;
                case NUMBER:
                    System.out.println("There is a number here!");
                    break;
            }
        }
    }

    private static Command getCommand(Scanner scanner) {
        System.out.print("Set/unset mines marks or claim a cell as free: ");
        String commandLine = scanner.nextLine();
        String[] command = commandLine.split("\\s+");
        int x = Integer.parseInt(command[0]);
        int y = Integer.parseInt(command[1]);
        Coordinates coords = new Coordinates(y - 1, x - 1);

        return new Command(coords, command[2]);
    }
}

enum Status {IN_PROGRESS, NUMBER, WIN, LOSS}

enum CommandType {FREE, MINE}
