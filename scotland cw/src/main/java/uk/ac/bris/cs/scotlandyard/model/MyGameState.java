package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;

import java.util.List;
import java.util.Optional;

    final class MyGameState implements GameState {
    private GameSetup setup;
    private ImmutableSet<Piece> remaining;
    private ImmutableList<LogEntry> log;
    private Player mrX;
    private List<Player> detectives;
    private ImmutableList<Player> everyone;
    private ImmutableSet<Move> moves;
    private ImmutableSet<Piece> winner;

    MyGameState(final GameSetup setup,
                final ImmutableSet<Piece> remaining,
                final ImmutableList<LogEntry> log,
                final Player mrX,
                final List<Player> detectives){

        this.setup = setup;
        this.remaining = remaining;
        this.log = log;
        this.mrX = mrX;
        this.detectives = detectives;
    }
    @Override
    public GameSetup getSetup(){
        return this.setup;
    }
    @Override
    public ImmutableSet<Piece> getPlayers(){
        return null;
    }
    @Override
    public Optional<Integer> getDetectiveLocation(Piece.Detective detective){
        return null;
    }
    @Override
    public Optional<TicketBoard> getPlayerTickets(Piece piece){
        return null;
    }
    @Override
    public ImmutableList<LogEntry> getMrXTravelLog(){
        return this.log;
    }
    @Override
    public ImmutableSet<Piece> getWinner(){
        return null;
    }
    @Override
    public ImmutableSet<Move> getAvailableMoves(){
        return null;
    }
    @Override
    public GameState advance(Move move){
        return null;
    }

}
