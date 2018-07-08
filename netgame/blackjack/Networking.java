package netgame.blackjack;

import java.io.*;
import java.util.*;

import netgame.common.*;


public class Networking extends Hub
{
    private Deck deck = new Deck(); // The deck of 52 playing cards.

    private Hand[] hand = new Hand[3];

    private boolean[] noMoreDecision = new boolean[3];

    private boolean[] done = new boolean[3];

    private Hand dealerHand = new Hand();

    // private Hand[] extraHand = new Hand[4]; //extra hand for splitters

    private Queue<Integer> players = new LinkedList<Integer>(); // List of
                                                                // players.

    private final static int WAITING_FOR_BET = 0; // Hub is waiting for a player
                                                  // to hit.

    private final static int WAITING_FOR_CURRENT = 1;

    // private final static int WAITING_FOR_HIT = 5; // Hub is waiting for a
    // player to hit.
    // private final static int WAITING_FOR_STAND = 2; // Hub is waiting for a
    // player to stand.
    // private final static int WAITING_FOR_DOUBLE = 3; // Hub is waiting for a
    // player to double.
    // private final static int WAITING_FOR_SPLIT = 4; // Hub is waiting for a
    // player to split.
    // private final static int WAITING_FOR_SURRENDER = 5; // Hub is waiting for
    // a player to surrender.

    private int status; // The basic game status, one of the preceding 4 values,
                        // telling what message the hub is expecting.

    private int currentPlayer; // The ID number (1 or 2 or 3) player who is to
                               // send the next message.
                               // Note that the ID number of the opposing player
                               // is always 3-currentPlayer.

    private int numberOfPlayers; // The total # of players.

    private int[] money = new int[3]; // money[0] is the amount of money that
                                      // Player #1 has left;
                                      // money[1] is the amount of money that
                                      // Player #2 has left.
                                      // money[2] is the amount of money that
                                      // Player #3 has left.
                                      // These values are initialized to 1000 at
                                      // the start. They
                                      // can become negative, and nothing is
                                      // done about it if they do.

    private int[] bets = new int[3]; // amount each person bet

    private boolean firstMessage = true;
    

    /**
     * Constructor
     * @param port port
     * @throws IOException
     */
    public Networking( int port ) throws IOException
    {
        super( port );
    }


    /**
     * When the  players connects, this method starts the game by sending
     * the initial game state. At this time, the players'
     * hands are null. The hands will be set when the first hand is dealt.
     * 
     * @param playerID playerID
     */
    protected void playerConnected( int playerID )
    {
        if ( playerID == 1 )
        {
            players.add( 1 );
            money[0] = 1000;
            bets[0] = 0;
            hand[0] = new Hand();
            noMoreDecision[0] = false;
            sendToOne( 1, new Integer( 1 ) );
            sendToOne( 1, new GameState( null, GameState.BET, money[0], bets, null ) );
        }
        else if ( playerID == 2 )
        {
            players.add( 2 );
            money[1] = 1000;
            bets[1] = 0;
            hand[1] = new Hand();
            noMoreDecision[1] = false;
            sendToOne( 2, new Integer( 2 ) );
            sendToOne( 2, new GameState( null, GameState.BET, money[1], bets, null ) );
        }
        else if ( playerID == 3 )
        {
            shutdownServerSocket();
            players.add( 3 );
            money[2] = 1000;
            bets[2] = 0;
            hand[2] = new Hand();
            noMoreDecision[2] = false;
            sendToOne( 3, new Integer( 3 ) );
            sendToOne( 3, new GameState( null, GameState.BET, money[2], bets, null ) );
        }
        status = WAITING_FOR_BET;

    }


    /**
     * If a player disconnects, the game ends. This method shuts down the Hub,
     * which will send a signal to the remaining connected player, if any, to
     * let them know that their opponent has left the game. The client will
     * respond by terminating that player's program.
     * 
     * @param playerID playerID
     */
    protected void playerDisconnected( int playerID )
    {
        int i = 1;
        while ( i <= players.size() )
        {
            players.remove(i);
            i++;
        }
        
        shutDownHub();
    }


    /**
     * 
     * checks to see if everyone has bet
     * 
     * @return if everyone has bet.
     */
    private boolean everyoneHasBet()
    {
        for ( int player : players )
        {
            if ( bets[player - 1] == 0 )
            {
                return false;
            }
        }
        return true;
    }


    /**
     * This is the method that responds to messages received from the clients.
     * It handles all of the action of the game. When a message is received,
     * this method will make any changes to the state of the game that are
     * triggered by the message. It will then send information about the new
     * state to each player, and it will generally send a string to each client
     * as a message to be displayed to that player.
     * 
     * @param playerID playerID
     * @param message message that the player sends
     */
    protected void messageReceived( int playerID, Object message )
    {
        if ( firstMessage )
        {
            shutdownServerSocket();
            currentPlayer = players.peek();
            players.add( players.remove() );
            numberOfPlayers = players.size();
            firstMessage = false;
        }

        if ( message instanceof Integer ) // A bet.
        {
            if ( status != WAITING_FOR_BET )
            {
                System.out.println( "Error: BET message received at incorrect time." );
                return;
            }
            else if ( bets[playerID - 1] != 0 )
            {
                System.out.println( "Error: Player has already bet." );
                return;
            }

            int bet = ( (Integer)message ).intValue();

            if ( bet < 0 )
            {
                System.out.println( "Error: BET needs to be greater than 0." );
                return;
            }
            if ( bet > money[playerID - 1] )
            {
                sendToOne( playerID, "OverBetting" );
                return;
            }
            else
            {
                done[playerID - 1] = false;
                money[playerID - 1] -= bet;
                bets[playerID - 1] = bet;
                sendToOne( playerID, "You bet $" + bet ); // message
                sendToOthers( playerID, "Player number " + playerID + " bets $" + bet ); // message

                // update gamestate for each player
                sendBettingState();
                if ( everyoneHasBet() )
                {
                    // deal cards
                    sendToAll( "Everyone gets 2 cards." );
                    deck.shuffle(); // shuffle the deck
                    for ( int player = 1; player <= numberOfPlayers; player++ )
                    {
                        hand[player - 1].deal( deck );
                        hand[player - 1].deal( deck ); // two cards
                    }
                    dealerHand.deal( deck );
                    dealerHand.deal( deck );
                    try
                    {
                        sendToAll( dealerHand.clone() );
                    }
                    catch ( CloneNotSupportedException e )
                    {
                    } 
                    // send dealers cards to
                                         // everyone
                    sendToAll( "Cards have been dealt. Player " + playerID + " make your decision." );
                    status = WAITING_FOR_CURRENT;
                    for (int i = 0; i < numberOfPlayers; i++)
                    {
                        if (hand[i].getTotal() == 21)
                        {
                            noMoreDecision[i] = true;
                            sendToOne(i+1, "First BJ");
                        }
                    }
                    if (money[playerID-1] < bets[playerID-1] )
                    {
                        sendToOne(playerID, "Not enough money");
                    }
                    sendState( GameState.MAKE_DECISION );
                    return;
                }
                return;
            }
        }
        else
        {
            if ( message.equals( "hit" ) )
            {
                hand[currentPlayer - 1].deal( deck );
                sendToAll( "Player " + currentPlayer + " hit." );
            }
            else if ( message.equals( "stand" ) )
            {
                sendToAll( "Player " + currentPlayer + " stands." );
                noMoreDecision[currentPlayer - 1] = true;
            }
            else if ( message.equals( "double" ) )
            {
                int bet = bets[playerID - 1];
                money[playerID - 1] -= bet;
                bets[playerID - 1] += bet;

                hand[currentPlayer - 1].deal( deck );

                sendToAll( "Player " + currentPlayer + " doubles down." );
                noMoreDecision[currentPlayer - 1] = true;
            }
            else if ( message.equals( "surrender" ) )
            {
                int halfOfBet = bets[playerID - 1] / 2;
                money[playerID - 1] += halfOfBet;
                bets[playerID - 1] = 0;

                hand[currentPlayer - 1].remove( 1 ); // remove the hands so that
                                                     // I
                hand[currentPlayer - 1].remove( 0 ); // can categorize the loser
                                                     // in
                                                     // round total

                sendToAll( "Player " + currentPlayer + " surrenders." );
                noMoreDecision[currentPlayer - 1] = true;
            }
            else if ( message.equals( "Done" ) )
            {
                done[playerID-1] = true;
                if ( isEveryoneDone() )
                {
                    sendToAll("New Game");
                    for ( int i = 0; i < 3; i++ )
                    {
                        bets[i] = 0;
                    }

                    hand = new Hand[3]; // clear the hands
                    dealerHand = new Hand(); // clear the dealers hands
                    firstMessage = true;
                    for ( int i = 1; i <= numberOfPlayers; i++ )
                    {
                        noMoreDecision[i - 1] = false;
                    }

                    status = WAITING_FOR_BET;
                    for ( int ID = 1; ID <= numberOfPlayers; ID++ )
                    {
                        hand[ID - 1] = new Hand();
                        sendToOne( ID, new GameState( null, GameState.BET, money[ID - 1], bets, hand ) );
                    }
                }
                return;
            }

            checkBust();

            if ( everyoneDone() )
            {
                sendToAll("everyone done");
                changeBackToPlayerOne();

                sendState( GameState.WAIT_FOR_DECISION ); // change state
                roundOver();
                return;
            }
            else
            {
                moveOnToNext();

                sendToAll( "Player " + currentPlayer + " make your decision." );
                sendState( GameState.MAKE_DECISION ); // change state
                status = WAITING_FOR_CURRENT;
            }

        }
    }


    /**
     * Checks to see if the round is over.
     * @return true or false
     */
    private boolean isEveryoneDone()
    {
        for ( int i = 0; i < numberOfPlayers; i++ )
        {
            if ( done[i] == false )
            {
                return false;
            }
        }
        return true;
    }


    /**
     * Checks to see if anyone has busted.
     */
    private void checkBust()
    {
        int total = hand[currentPlayer - 1].getTotal();
        if ( total >= 21 )
        {
            noMoreDecision[currentPlayer - 1] = true;
        }
    }


    /**
     * Checks to see if everyone has made their decision.
     * @return true or false
     */
    private boolean everyoneDone()
    {
        boolean ans = true;
        for ( int ID = 1; ID <= numberOfPlayers; ID++ )
        {
            if ( noMoreDecision[ID - 1] == false )
            {
                ans = false;
            }
        }
        return ans;
    }


    /**
     * Moves to the next open player.
     */
    private void moveOnToNext()
    {
        currentPlayer = players.peek(); // have to move on to the next player
                                        // once
        players.add( players.remove() ); // before iterating to check others
       

        while ( noMoreDecision[currentPlayer - 1] == true )
        {
            currentPlayer = players.peek();
            players.add( players.remove() );
        }
        
        if (hand[currentPlayer-1].size()> 2)
        {
            sendToOne(currentPlayer, "Not first decision");
        }
    }


    /**
     * Changes back to player 1 for the next round.
     */
    private void changeBackToPlayerOne()
    {
        while ( currentPlayer != 1 )
        {
            currentPlayer = players.peek();
            players.add( players.remove() );
        }
    }


    /**
     * This method is called when the game ends and there is a winner. It gives
     * the pot to the winner, sends each player a message about the outcome of
     * the game, changes the state to get ready for the next game, and sends a
     * state message to each player.
     * 
     */
    private void roundOver()
    {
        ArrayList<Integer> losers = new ArrayList<Integer>();
        ArrayList<Integer> pushers = new ArrayList<Integer>(); // when they tie
        ArrayList<Integer> winners = new ArrayList<Integer>();

        int dealerTotal = dealerHand.getTotal();

        while ( dealerTotal < 17 )
        {
            dealerHand.deal( deck );
            dealerTotal = dealerHand.getTotal();
            try
            {
                sendToAll( dealerHand.clone() );
            }
            catch ( CloneNotSupportedException e )
            {
            }
        }

        if ( dealerTotal > 21 ) // if dealer busts
        {
            int total = 0;
            for ( int ID = 1; ID <= numberOfPlayers; ID++ )
            {

                total = hand[ID - 1].getTotal();

                if ( total == 0 )
                {
                    losers.add( ID );
                }
                else if ( total > 21 )
                {
                    pushers.add( ID );
                    money[ID - 1] += bets[ID - 1];
                    System.out.println( "Bust: push" );
                }
                else
                {
                    winners.add( ID );
                    money[ID - 1] += bets[ID - 1] * 2;
                    System.out.println( "Bust: won" );
                }
            }

        }
        else // if dealer doesn't bust
        {
            int total = 0;
            for ( int ID = 1; ID <= numberOfPlayers; ID++ )
            {
                total = hand[ID - 1].getTotal();

                if ( total == 0 )
                {
                    losers.add( ID );
                }
                else if ( total > 21 )
                {
                    losers.add( ID );
                    System.out.println( "Not Bust: lost 1" );
                }
                else if ( dealerTotal > total )
                {
                    losers.add( ID );
                    System.out.println( "Not Bust: lost 2" );
                }
                else if ( dealerTotal == total )
                {
                    pushers.add( ID );
                    money[ID - 1] += bets[ID - 1];
                    System.out.println( "Not Bust: push" );
                }
                else if ( dealerTotal < total )
                {
                    winners.add( ID );
                    money[ID - 1] += bets[ID - 1] * 2;
                    System.out.println( "Not Bust: won" );
                }
            }
        }

        for ( int i = 1; i <= numberOfPlayers; i++ )
        {
            if ( money[i - 1] < 100 )
            {
                sendToOne( i, "poor" );
                return;
            }
        }

        // send out messages and start new round
        for ( int player : winners )
        {
            sendToOne( player, "You win." );
        }
        for ( int player : losers )
        {
            sendToOne( player, "You lose." );
        }
        for ( int player : pushers )
        {
            sendToOne( player, "It's a push." );
        }
    }


    /**
     * Changes a hand to an array.
     * @param h Hand
     * @return changes to array
     */
    private Card[] changeToArray( Hand h )
    {
        Card[] array = new Card[h.size()];

        for ( int i = 0; i < h.size(); i++ )
        {
            array[i] = h.get( i );
        }

        return array;
    }


    /**
     * This method is used by messageReceived() to send state messages to the
     * rest of the players.
     *
     * @param currentPlayerID
     *            The state of the player who makes the next move. One of the
     *            status values from the PokerGameState class.
     * @param message
     *            The state of the opposing player. One of the status values
     *            from the PokerGameState class.
     */
    private void sendToOthers( int currentPlayerID, Object message )
    {
        if ( currentPlayerID < 1 || currentPlayerID > numberOfPlayers )
        {
            System.out.println( "Error: player does not exist." );
            return;
        }
        if ( numberOfPlayers > 1 )
        {
            for ( int oppID = 1; oppID <= numberOfPlayers; oppID++ )
            {
                if ( oppID != currentPlayerID )
                {
                    sendToOne( oppID, message );
                }
            }
        }
    }


    /**
     * This method is used by messageReceived() to send state messages to both
     * players.
     * 
     * @param currentPlayerState
     *            The state of the player who makes the next move. One of the
     *            status values from the PokerGameState class.
     * @param opponentState
     *            The state of the opposing player. One of the status values
     *            from the PokerGameState class.
     */
    private void sendBettingState()
    {
        for ( int ID = 1; ID <= numberOfPlayers; ID++ )
        {
            if ( bets[ID - 1] != 0 || ID == currentPlayer )
            {
                sendToOne( ID, new GameState( null, GameState.DONE_BET, money[ID - 1], bets.clone(), null ) );
            }
            else
            {
                sendToOne( ID, new GameState( null, GameState.BET, money[ID - 1], bets.clone(), null ) );
            }
        }
    }


    /**
     * This method is used by messageReceived() to send state messages to both
     * players.
     * 
     * @param currentPlayerState
     *            The state of the player who makes the next move. One of the
     *            status values from the PokerGameState class.
     * @param opponentState
     *            The state of the opposing player. One of the status values
     *            from the PokerGameState class.
     */
    private void sendState( int currentPlayerState )
    {
        if ( currentPlayer < 1 || currentPlayer > numberOfPlayers )
        {
            System.out.println( "Error: player does not exist." );
            return;
        }
        if ( numberOfPlayers == 1 )
        {
            Card[] send = changeToArray( hand[0] );
            sendToOne( currentPlayer,
                new GameState( send.clone(), currentPlayerState, money[0], bets.clone(), hand.clone() ) );
        }
        else // more than 1 player
        {
            for ( int ID = 1; ID <= numberOfPlayers; ID++ )
            {
                if ( ID == currentPlayer ) // if current player
                {
                    Card[] send = changeToArray( hand[ID - 1] );
                    sendToOne( ID,
                        new GameState( send.clone(), currentPlayerState, money[ID - 1], bets.clone(), hand.clone() ) );
                }
                else // if others
                {
                    Card[] send = changeToArray( hand[ID - 1] );
                    sendToOne( ID,
                        new GameState( send.clone(),
                            GameState.WAIT_FOR_DECISION,
                            money[ID - 1],
                            bets.clone(),
                            hand.clone() ) );
                }
            }
        }
    }
    
    //------------------------- for testing ---------------------------------------
    
    
    public boolean[] returnDecisions()
    {
        return noMoreDecision;
    }
    
    public Hand[] getHand()
    {
        return hand;
    }
    
    public String handsNullCheck( )
    {
        String ans = "";
        
        if(hand == null)
        {
            ans = "null";
        }
        else
        {           
            for(int i = 0; i < hand.length; i++)
            {
                ans += "Player " + i + " has a hand";
            }
        }
        
        return ans;
    }
    
    public String moneyToString( )
    {
        String ans = "Players money: ";
        for(int i: money)
        {
            ans += i + " ";
        }
        return ans;
    }
    public String playersToString( )
    {
        return currentPlayer + "";
    }
    
    public String returnPlayers( )
    {
        Queue<Integer> list = players;
        String ans = "Players: ";
        
        if ( players.size() != 0 )
        {
            for ( Integer k : list )
            {
                ans += k + " ";
            }
        }
        
        return ans;
    }
    
    public String betsToString( )
    {
        String ans = "Players bets: ";
        
        for(int k: bets)
        {
            ans += k + " ";
        }
        
        return ans;
    }
}