package netgame.blackjack;

import java.io.Serializable;


/**
 * TODO Write a one-sentence summary of your class here. TODO Follow it with
 * additional details about its purpose, what abstraction it represents, and how
 * to use it.
 *
 * @author dgoel688
 * @version May 19, 2016
 * @author Period: TODO
 * @author Assignment: BlackJack
 *
 * @author Sources: TODO
 */
public class Card implements Serializable
{

    public final static int CLUB = 1;

    public final static int DIAMOND = 2;

    public final static int HEART = 3;

    public final static int SPADE = 4;

    public final static int ACE = 1;

    public final static int JACK = 11;

    public final static int QUEEN = 12;

    public final static int KING = 13;

    private int number;

    private int suit;


    /**
     * Creates a card with a specified suit and value.
     * 
     * @param num
     *            the value of the new card. For a regular card , the value must
     *            be in the range 2 through 14, with 14 representing an Ace. You
     *            can use the constants PokerCard.ACE, PokerCard.QUEEN, and
     *            PokerCard.KING.
     * @param mark
     *            the suit of the new card. This must be one of the values
     *            PokerCard.SPADES, PokerCard.HEARTS, PokerCard.DIAMONDS,
     *            PokerCard.CLUBS, or PokerCard.JOKER.
     * @throws IllegalArgumentException
     *             if the parameter values are not in the Permissible ranges
     * 
     */
    public Card( int num, int mark )
    {
        if ( mark != SPADE && mark != HEART && mark != DIAMOND && mark != CLUB )
            throw new IllegalArgumentException( "Illegal playing card suit" );
        if ( ( num < 1 || num > 13 ) )
            throw new IllegalArgumentException( "Illegal playing card value" );
        number = num;
        suit = mark;
    }


    /**
     * TODO Write your method description here.
     * 
     * @return
     */
    public int getNumber()
    {
        return number;
    }


    /**
     * TODO Write your method description here.
     * 
     * @return
     */
    public int getSuit()
    {
        return suit;
    }


    /**
     * TODO Write your method description here.
     * 
     * @param num
     * @return
     */
    public int getValue()
    {
        switch ( number )
        {
            case 1:
                return 1;
            case 2:
                return 2;
            case 3:
                return 3;
            case 4:
                return 4;
            case 5:
                return 5;
            case 6:
                return 6;
            case 7:
                return 7;
            case 8:
                return 8;
            case 9:
                return 9;
            case 10:
                return 10;
            case 11:
                return 10;
            case 12:
                return 10;
            case 13:
                return 10;
            default:
                return 11;
        }

    }


    /**
     * Returns a String representation of the card's suit.
     * 
     * @return one of the strings "Spades", "Hearts", "Diamonds",or "Clubs".
     */
    public String getSuitAsString()
    {
        switch ( suit )
        {
            case SPADE:
                return "Spades";
            case HEART:
                return "Hearts";
            case DIAMOND:
                return "Diamonds";
            case CLUB:
                return "Clubs";
        }
        return null;
    }


    /**
     * Returns a String representation of the card's value.
     * 
     * @return for a regular card, one of the strings "2", "3", ..., "10",
     *         "Jack", "Queen", "King" or "Ace". For a Joker, the string is
     *         always numerical.
     */
    public String getValueAsString()
    {

        switch ( number )
        {
            case 2:
                return "2";
            case 3:
                return "3";
            case 4:
                return "4";
            case 5:
                return "5";
            case 6:
                return "6";
            case 7:
                return "7";
            case 8:
                return "8";
            case 9:
                return "9";
            case 10:
                return "10";
            case 11:
                return "Jack";
            case 12:
                return "Queen";
            case 13:
                return "King";
            default:
                return "Ace";
        }

    }


    /**
     * Returns a string representation of this card, including both its suit and
     * its value . Sample return values are: "Queen of Hearts", "10 of Diamonds"
     * , "Ace of Spades",
     */
    public String toString()
    {
        return getValueAsString() + " of " + getSuitAsString();
    }
}
