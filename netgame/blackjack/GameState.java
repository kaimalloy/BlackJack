package netgame.blackjack;

import java.io.Serializable;


/**
 * Represents the state of a game of five-card-draw poker from one player's
 * point of view. The full state of a game is kept by a PokerHub. That hub sends
 * messages of type PokerGameState to each player whenever the state of the game
 * changes. Note that the two players receive different messages, to reflect
 * each player's view of the status of the game.
 */
public class GameState implements Serializable
{

    // -------------------------------------------------------------
    // The eight following constants are the possible values of
    // status. The status is the basic information that tells
    // a player what it should be doing at a given time.

    public final static int BET = 0;

    public final static int DONE_BET = 1;
    // public final static int HIT = 1;
    // public final static int STAND = 2;
    // public final static int DOUBLE = 3; // Hub is waiting for a player to
    // double.
    // public final static int SPLIT = 4; // Hub is waiting for a player to
    // split.
    // public final static int SURRENDER = 5; // Hub is waiting for a player to
    // surrender.

    public final static int MAKE_DECISION = 2; // Wait for opposing player to
                                               // start the game.

    public final static int WAIT_FOR_DECISION = 3; // Wait for opposing player
                                                   // to BET (or fold).

    public final static int DONE = 4;

    // -------------------------------------------------------------
    public final Card[] hand; // Player's hand; null before game starts.
    
    public int status; // Game status; one of the constants defined in this
                       // class.

    public int money; // Amount of money that player has left.

    public int[] betting; // Amount of money that player bets

    public Hand[] allHands;


    /**
     * Create a PokerGameState object with specified values for all public
     * variables in this class.
     */
    public GameState( Card[] hand, int status, int money, int[] betting, Hand[] allHands )
    {
        this.hand = hand;
        this.status = status;
        this.money = money;
        this.betting = betting;
        this.allHands = allHands;
    }
    
    
    public String toString()
    {
        String ans = "";
        ans+= " Players hand: ";
        for(Card c: hand)
        {
            ans += c.toString() + ", ";
        }
        ans+= " Players status: " + status;
        ans+= " Players money: " + money;
        ans+= " Players bet: ";
        
        for(int k : betting)
        {
            ans += k + ", ";
        }
        ans+= "Other players hands: ";
        for(Hand h : allHands)
        {
            for(Card c: hand)
            {
                ans += c.toString() + ", ";
            }
        }
        ans = ans.substring( 0, ans.length() - 2 );
        return ans;
    }
}
