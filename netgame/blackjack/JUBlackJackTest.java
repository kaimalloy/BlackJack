package netgame.blackjack;
import java.io.IOException;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.regex.*;
import org.junit.*;

import static org.junit.Assert.*;
import junit.framework.JUnit4TestAdapter;


/**
 * BlackJack tests: Card Hand Deck Networking Gamestate BJWindow Main
 * Stock
 *  BlackJack tests:
 *   Card             -Devansh Goel
 *   Hand             -Devansh Goel
 *   Deck             -Devansh Goel
 *   Networking       -Kai Malloy
 *   GameState        -Kai Malloy
 *   BJWindow         -Yuta Tsumori
 *    Main            -Yuta Tsumori
 *    
 *
 * @author Devansh Goel
 * @author Kai Malloy
 * @author Yuta Tsumori
 * @version 5/30/16
 * 
 * @author Sources: none
 *
 */
public class JUBlackJackTest
{
    // Test Card
    /**
     * Card tests:
     *   CardConstructor - constructs Card and then compare toString
     *   CardGetNumber - compares number returned to constructed number
     *   CardGetSuit - compares suit returned to constructed suit
     *   CardGetValue - compares value returned to constructed value
     *   CardGetSuitAsString - compares suit as string returned to constructed suit
     *   CardGetValueAsString - compares value as string returned to constructed value
     */
    private int num = 1;
    private int suit = 4;
    @Test
    public void CardConstructor()
    {
        Card card = new Card( num, suit );
        String card1 = card.toString();
        assertTrue( card1.contains( "Ace of Spades" ) );
    }


    @Test
    public void CardGetNumber()
    {
        Card card = new Card( num, suit );
        assertTrue( card.getNumber() == num );
    }


    @Test
    public void CardGetSuit()
    {
        Card card = new Card( num, suit );
        assertTrue( card.getSuit() == suit );
    }


    @Test
    public void CardgetValue()
    {
        Card card = new Card( num, suit );
        assertFalse( card.getValue() == 11 );
    }


    @Test
    public void CardGetSuitAsString()
    {
        Card card = new Card( num, suit );
        String str = card.getSuitAsString();
        assertTrue( str.contains( "Spades" ) );
    }


    @Test
    public void CardGetValueAsString()
    {
        Card card = new Card( num, suit );
        String str = card.getValueAsString();
        assertTrue( str.contains( "Ace" ) );
    }


    // Test Hand
    /**
     * Hand tests:
     *   HandConstructor - constructs Hand and then compare to null
     *   HandDeal - compares size returned to 1
     *   HandIsEmpty - compares empty hand to true
     *   HandAdd - compares size returned to one
     *   HandGet - compares card returned to null
     *   HandRemove - compares size returned to 0
     *   HandSize - compares size returned to 0
     *   HandGetTotal - compares total returned to total >= 4 || total <= 21
     */
    @Test
    public void HandConstructor()
    {
        Hand hand = new Hand();
        assertNotNull( hand );
    }


    @Test
    public void HandDeal()
    {
        Deck deck = new Deck();
        Hand hand = new Hand();
        hand.deal( deck );
        int size = hand.size();
        assertTrue( size == 1 );
    }


    @Test
    public void HandIsEmpty()
    {
        Hand hand = new Hand();
        boolean empty = hand.isEmpty();
        assertTrue( empty == true );
    }


    @Test
    public void HandAdd()
    {
        Hand hand = new Hand();
        Card card = new Card( num, suit );
        hand.add( card );
        int size = hand.size();
        assertTrue( size == 1 );
    }


    @Test
    public void HandGet()
    {
        Card card = new Card( num, suit );
        Hand hand = new Hand();
        hand.add( card );
        Card card1 = hand.get( 0 );
        assertNotNull( card1 );
    }


    @Test
    public void HandRemove()
    {
        Card card = new Card( num, suit );
        Hand hand = new Hand();
        hand.add( card);
        hand.remove( 0 );
        int size = hand.size();
        assertTrue( size == 0 );
    }


    @Test
    public void HandSize()
    {
        Hand hand = new Hand();
        int size = hand.size();
        assertTrue( size == 0 );
    }


    @Test
    public void HandGetTotal()
    {
        Hand hand = new Hand();
        int total = hand.getTotal();
        assertTrue( total >= 4 || total <= 21 );
    }


    // Test Deck
    /**
     * Hand tests:
     *   DeckConstructor - constructs Deck and then compare to null
     *   DeckShuffle - compares deck to null
     *   DeckCardsLeft - compares full deck to size of 52
     *   DeckDealCard - compares deck missing a card to size of 51
     */
    @Test
    public void DeckConstructor()
    {
        Deck deck = new Deck();
        assertNotNull( deck );
    }


    @Test
    public void DeckShuffle()
    {
        Deck deck = new Deck();
        deck.shuffle();
        assertNotNull( deck );
    }


    @Test
    public void DeckCardsLeft()
    {
        Deck deck = new Deck();
        int cards = deck.cardsLeft();
        assertTrue( cards == 52 );
    }


    @Test
    public void DeckDealCard()
    {
        Deck deck = new Deck();
        deck.dealCard();
        int cards = deck.cardsLeft();
        assertTrue( cards == 51 );
    }
    /**
     * Networking tests:
     *   NetworkingConstructor - constructs Networking and then compare null
     *   NetworkingPlayerConnected - compares connected players to connected players returned
     *   NetworkingPlayerDisconnected - compares disconnected players to disconnected players returned
     *   NetworkingMessageReceived - compares message received to message returned
     */
    
    private int playerID1 = 1;
    private int playerID2 = 2;
    private int playerID3 = 3;
    private int DEFAULT_PORT = 32058;
    
    @Test
    public void NetworkingConstructor() throws IOException
    {
        Networking net = new Networking(DEFAULT_PORT);
                
        assertNotNull("<< Invalid Networking Constructor >>",
            net );
        
        net.shutDownHub();
    }
    @Test
    public void NetworkingPlayerConnected() throws IOException
    {
        Networking net = new Networking(DEFAULT_PORT);
        net.playerConnected( playerID1 );
        
        String players = net.returnPlayers();
        String money = net.moneyToString();
        String bets = net.betsToString();
        Hand[] hand = net.getHand();
        
        assertTrue("<< Invalid Networking Player Connected >>",
            players.contains( "" + playerID1) );
        
        assertTrue("<< Invalid Networking Player Connected >>",
            money.contains( "" + 1000) );
        
        assertTrue("<< Invalid Networking Player Connected >>",
            bets.contains( "" + 0) );
        
        assertNotNull("<< Invalid Networking Player Connected >>",
            hand);
        
        net.shutDownHub();
    }
    @Test
    public void NetworkingPlayerDisconnected() throws IOException
    {
        Networking net = new Networking(DEFAULT_PORT);
        net.playerConnected( playerID1 );
        net.playerConnected( playerID2 );
        net.playerDisconnected( playerID1 );
        
        String players = net.returnPlayers();
        
        assertTrue("<< Invalid Networking Player Disconected>>",
           players.equals("Players: 2 ") );
        
        net.shutDownHub();
    }
    @Test
    public void NetworkingMessageReceived() throws IOException
    {
        Networking net = new Networking(DEFAULT_PORT);
        net.playerConnected( playerID1 );
        net.playerConnected( playerID2 );
        net.playerConnected( playerID3 );
        
        net.messageReceived( playerID1, 200 );
        
        String bets = net.betsToString();
        
        assertTrue("<< Invalid Networking Message Received>>",
            bets.contains("" + 200) );
        
        net.messageReceived( playerID1, "hit" );
        
        Hand[] h = net.getHand();
        
        assertNotNull("<< Invalid Networking Message Received>>",
            h[0].get( 0 ) );
        
        net.messageReceived( playerID1, "stand" );
        
        boolean[] test = net.returnDecisions();
        boolean tf = false;
        for(boolean b: test)
        {
            if(b)
            {
                tf = true;
            }
        }
        
        assertTrue("<< Invalid Networking Message Received>>",
            tf );    
        
        net.messageReceived( playerID1, "double" );
        
        String bet = net.betsToString();
        
        
        assertNotNull("<< Invalid Networking Message Received>>",
            bet.contains( "400" ) );
        
        net.shutDownHub();
    }
    

    /**
     * Gamestate tests:
     *   GameStateConstructor - constructs GameState and then compare toString
     */
    private Card card1 = new Card( 1, Card.DIAMOND );
    private Card card2 = new Card( 2, Card.DIAMOND );
    private Card card3 = new Card( 3, Card.DIAMOND );
    private int status = GameState.BET;
    private int money = 1000;
   
    @Test
    public void GameState()
    {
        Card[] hand = new Card[3];
        hand[0] = card1;
        hand[1] = card2;
        hand[2] = card3;
        
        Hand[] allHands = new Hand[3];
        allHands[0] = new Hand();
        allHands[0].add( card1 );
        allHands[0].add( card2 );
        allHands[0].add( card3 );
        
        int[] betting = new int[3];
        betting[0] = 1000;
        
        GameState gs = new GameState(hand,status,money,betting,allHands);
        
        String toStr = gs.toString();
        
        assertNotNull( "<< Invalid GameState Constructor >>", hand );
        assertTrue( "<< Invalid GameState Constructor >>",
                toStr.contains( "Players status: " + status )
                && toStr.contains( "Players money: " + money ));
        assertTrue("<< Invalid GameState Constructor >>",
            toStr.contains( "Players hand: "));
        assertNotNull( "<< Invalid GameState Constructor >>", betting );
        assertNotNull( "<< Invalid GameState Constructor >>", allHands );
    }
    /**
     * BJWindow tests:
     *   CardConstructor - constructs Card and then compare toString
     *   CardGetNumber - compares number returned to constructed number
     *   CardGetSuit - compares suit returned to constructed suit
     *   CardGetValue - compares value returned to constructed value
     *   CardGetSuitAsString - compares suit as string returned to constructed suit
     *   CardGetValueAsString - compares value as string returned to constructed value
     */
    @Test
    public void BJWindow()
    {
        
    }
    /**
     * Main tests:
     *   CardConstructor - constructs Card and then compare toString
     *   CardGetNumber - compares number returned to constructed number
     *   CardGetSuit - compares suit returned to constructed suit
     *   CardGetValue - compares value returned to constructed value
     *   CardGetSuitAsString - compares suit as string returned to constructed suit
     *   CardGetValueAsString - compares value as string returned to constructed value
     */
    @Test
    public void Main()
    {
        
    }
}