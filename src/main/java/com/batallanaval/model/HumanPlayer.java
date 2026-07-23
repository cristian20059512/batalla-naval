package com.batallanaval.model;

/**
 * Human player. Their own board is the "placement board": observation
 * only, it reflects their fleet and the shots they have received.
 */
public class HumanPlayer extends Player {

    private static final long serialVersionUID = 1L;

    public HumanPlayer(String nickname, Board positionBoard) {
        super(nickname, positionBoard);
    }
}
