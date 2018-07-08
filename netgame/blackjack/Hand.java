package netgame.blackjack;

import java.io.Serializable;
import java.util.ArrayList;


/**
 * It is the hand class
 *
 * @author dgoel688
 * @version May 19, 2016
 * @author Period: TODO
 * @author Assignment: BlackJack
 *
 * @author Sources: TODO
 */
public class Hand implements Serializable, Cloneable
{
    public ArrayList<Card> hand;


    /**
     * @param deck
     */
    public Hand()
    {
        hand = new ArrayList<Card>();
    }


    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }


    /**
     * This deals a card from the deck to the hand
     * 
     * @param deck this is a deck object
     */
    public void deal( Deck deck )
    {
        hand.add( deck.dealCard() );
    }


    /**
     * This checks if the hand is empty
     * 
     * @return true if empty, false if not empty
     */
    public boolean isEmpty()
    {
        if ( hand.isEmpty() )
        {
            return true;
        }
        else
        {
            return false;
        }
    }


    /**
     * This adds a specific card c to the hand
     * 
     * @param c this is a card c.
     */
    public void add( Card c )
    {
        hand.add( c );
    }


    /**
     * This gets the card at pos c from the hand
     * 
     * @return the card at pos c from the hand
     * @param c this is the position.
     */
    public Card get( int c )
    {
        return hand.get( c );
    }


    /**
     * This removes the card from pos c
     * 
     * @return returns the card that is removed.
     * @param c the pos of the card
     */
    public Card remove( int c )
    {
        return hand.remove( c );
    }


    /**
     * Gives the size of the hand
     * 
     * @return size of the hand
     */
    public int size()
    {
        return hand.size();
    }


    /**
     * Finds the total value of the hand that is used in the game to compare
     * hands with the dealer and the player.
     * 
     * 
     * @return gives the value of the hand.
     */
    public int getTotal()
    {
        ArrayList<Card> temp = new ArrayList<Card>();
        int result = 0;
        int hasAce = 0;
        for ( Card c : hand )
        {
            if ( c.getValue() == 1 )
            {
                hasAce++;
            }
            else
            {
                temp.add( c );
            }
        }
        for ( Card c : temp )
        {
            result += c.getValue();
        }
        if ( result + 11 + hasAce - 1 <= 21 )
        {
            return result + 11 + hasAce - 1;
        }
        else
        {
            return result + hasAce;
        }
    }
}
