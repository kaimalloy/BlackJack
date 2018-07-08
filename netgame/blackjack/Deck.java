package netgame.blackjack;


/**
 *  TODO Write a one-sentence summary of your class here.
 *  TODO Follow it with additional details about its purpose, what abstraction
 *  it represents, and how to use it.
 *
 *  @author  dgoel688
 *  @version May 19, 2016
 *  @author  Period: TODO
 *  @author  Assignment: BlackJack
 *
 *  @author  Sources: TODO
 */
public class Deck
{
    private Card[] deck;

    
    private int numCardUsed;

    
    /**
     * Constructs a poker deck of playing cards, The deck contains
     * the usual 52 cards.   Initially the cards
     * are in a sorted order.  The shuffle() method can be called to
     * randomize the order.
     * 
     */
    public Deck()
    {
        deck = new Card[52];
        int count = 0;
        for ( int suits = 1; suits <= 4; suits++ )
        {
            for ( int val = 1; val <= 13; val++ )
            {
                deck[count] = new Card( val, suits );
                count++;
            }
        }
        numCardUsed = 0;
    }

    /**
     * Put all the used cards back into the deck (if any), and
     * shuffle the deck into a random order.
     */
    public void shuffle()
    {
        for ( int i = deck.length - 1; i > 0; i-- )
        {
            int rand = (int)( Math.random() * ( i + 1 ) );
            Card temp = deck[i];
            deck[i] = deck[rand];
            deck[rand] = temp;
        }
        numCardUsed = 0;
    }
    
   
    
    /**
     * As cards are dealt from the deck, the number of cards left
     * decreases.  This function returns the number of cards that
     * are still left in the deck.  The return value would be
     * 52 when the deck is first created or after the deck has been
     * shuffled.  It decreases by 1 each time the dealCard() method
     * is called.
     */
    public int cardsLeft() {
        return deck.length - numCardUsed;
    }

   

    /**
     * Removes the next card from the deck and return it.  It is illegal
     * to call this method if there are no more cards in the deck.  You can
     * check the number of cards remaining by calling the cardsLeft() function.
     * @return the card which is removed from the deck.
     * @throws IllegalStateException if there are no cards left in the deck
     */
    public Card dealCard() {
        if (numCardUsed == deck.length)
            throw new IllegalStateException("No cards are left in the deck.");
        numCardUsed++;
        return deck[numCardUsed - 1];
        // Programming note:  Cards are not literally removed from the array
        // that represents the deck.  We just keep track of how many cards
        // have been used.
    }
    
    
}


