package com.batallanaval.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A player in the game (human or machine). Holds their own board (where
 * their fleet is placed, either the human's placement board or the
 * machine's main board) and the history of shots they have fired at the
 * opponent's board.
 */
public abstract class Player implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String nickname;
    private final Board ownBoard;
    private final List<Shot> shotHistory = new ArrayList<>();

    protected Player(String nickname, Board ownBoard) {
        this.nickname = nickname;
        this.ownBoard = ownBoard;
    }

    public String getNickname() {
        return nickname;
    }

    public Board getOwnBoard() {
        return ownBoard;
    }

    public List<Shot> getShotHistory() {
        return shotHistory;
    }

    public void recordShot(Shot shot) {
        shotHistory.add(shot);
    }

    public int countOwnSunkShips() {
        return ownBoard.getFleet() == null
                ? 0
                : (int) ownBoard.getFleet().countSunkShips();
    }
}
