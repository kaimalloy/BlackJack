package netgame.blackjack;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import netgame.common.Client;
import netgame.blackjack.Card;
import netgame.blackjack.GameState;
import netgame.blackjack.BJWindow;


/**
 *  TODO Write a one-sentence summary of your class here.
 *  TODO Follow it with additional details about its purpose, what abstraction
 *  it represents, and how to use it.
 *
 *  @author  Yuta Tsumori
 *  @version Jun 1, 2016
 *  @author  Period: 5
 *  @author  Assignment: BlackJack
 *
 *  @author  Sources: None
 */
public class BJWindow extends JFrame
{

    private BJClient connection; // Handles communication with the PokerHub;
    // used to send messages to the hub.

    private GameState state; // Represents the state of the game, as seen
    // by this player. The state is
    // received as a message from the hub whenever
    // the state changes. This
    // variable changes only in the newState()
    // method.

    private int playerID;

    private int moneyInt;

    private int[] bettingInt;

    private Hand dealerHand; // The opponent's hand. This variable is
    // dull during the playing of a
    // hand. It becomes non-null if the
    // opponent's hand is sent to this
    // player at the end of one hand of poker.

    private Card[] playersHand;

    private Hand[] othersHand;

    private Display display; // The content pane of the window, defined by the
    // inner class, Display.

    private Image gameLogo, cardImages; // An image holding pictures of all the
                                        // cards. The
    // Image is loaded
    // as a resource by the PokerWindow constructor
    // from a resource file
    // "netgame/blackjack/cards.png." (The program
    // will be non-functional
    // if that resource file is not there.)

    private boolean firstDecision, enoughMoney, makingDecision, notBlackJack;

    private JRadioButton hundred, twoHundred, threeHundred, fourHundred, fiveHundred;

    private JButton hit, stand, doubleDown, surrender;

    private JButton quit;

    private JLabel messageFromServer;

    private JLabel ID, money;

    private JLabel total, otherTotal1, otherTotal2, dealerTotal, bet;


    public BJWindow( final String hubHostName, final int hubPort )
    {
        super( "BlackJack" );
        ClassLoader cl = getClass().getClassLoader();
        URL imageURL = cl.getResource( "netgame/blackjack/cards.png" );
        cardImages = Toolkit.getDefaultToolkit().createImage( imageURL );
        imageURL = cl.getResource( "netgame/blackjack/bjlogo.png" );
        gameLogo = Toolkit.getDefaultToolkit().createImage( imageURL );
        display = new Display();
        setContentPane( display );
        setResizable( false );
        setLocation( 200, 100 );
        ImageIcon img = new ImageIcon( "netgame/blackjack/playing_card_suits.png" );
        setIconImage( img.getImage() );
        setSize( 700, 600 );
        setLocation( 100, 100 );
        setVisible( true );
        setResizable( false );
        setDefaultCloseOperation( DO_NOTHING_ON_CLOSE );
        addWindowListener( new WindowAdapter()
        { // A listener to end the program when the user closes the window.
            public void windowClosing( WindowEvent evt )
            {
                doQuit();
            }
        } );
        setVisible( true );
        new Thread()
        { // A thread to open the connection to the server.
            public void run()
            {
                try
                {
                    final BJClient c = new BJClient( hubHostName, hubPort );
                    SwingUtilities.invokeLater( new Runnable()
                    {
                        public void run()
                        {
                            connection = c;
                            if ( c.getID() == 1 )
                            {
                                // This is Player #1. Still have to wait for
                                // second player to
                                // connect. Change the message display to
                                // reflect that fact.
                                messageFromServer.setText( "Waiting for an opponent to connect..." );
                            }
                        }
                    } );
                }
                catch ( final IOException e )
                {
                    // Error while trying to connect to the server. Tell the
                    // user, and end the program. Use
                    // SwingUtilties.invokeLater()
                    // because this happens in a thread other than the GUI event
                    // thread.
                    SwingUtilities.invokeLater( new Runnable()
                    {
                        public void run()
                        {
                            dispose();
                            JOptionPane.showMessageDialog( null,
                                "Could not connect to " + hubHostName + ".\nError:  " + e );
                            System.exit( 0 );
                        }
                    } );
                }
            }
        }.start();
        bettingWindow();
    }


    /**
     * A PokerClient is a netgame client that handles communication with the
     * PokerHub. It is used by the PokerWindow class to send messages to the
     * hub. When messages are received from the hub, it takes an appropriate
     * action.
     */
    private class BJClient extends Client
    {

        /**
         * Connect to a PokerHub at a specified hostname and port number.
         */
        public BJClient( String hubHostName, int hubPort ) throws IOException
        {
            super( hubHostName, hubPort );
        }


        /**
         * This method is called when a message from the hub is received by this
         * client. If the message is of type PokerGameState, then the newState()
         * method in the PokerWindow class is called to handle the change in the
         * state of the game. If the message is of type String, it represents a
         * message that is to be displayed to the user; the string is displayed
         * in the JLabel messageFromServer. If the message is of type
         * PokerCard[], then it is the opponent's hand. This had is sent when
         * the game has ended and the player gets to see the opponent's hand.
         * <p>
         * Note that this method is called from a separate thread, not from the
         * GUI event thread. In order to avoid synchronization issues, this
         * method uses SwingUtilties.invokeLater() to carry out its task in the
         * GUI event thread.
         */
        protected void messageReceived( final Object message )
        {
            SwingUtilities.invokeLater( new Runnable()
            {
                public void run()
                {
                    if ( message instanceof GameState )
                    {
                        newState( (GameState)message );
                        display.repaint();
                    }
                    else if ( message instanceof Integer )
                    {
                        playerID = ( (Integer)message ).intValue();
                        ID.setText( "Player " + playerID );
                    }
                    else if ( message instanceof String )
                    {
                        if ( ( (String)message ).equals( "OverBetting" ) )
                        {
                            bettingWindow();
                        }
                        else if ( ( (String)message ).equals( "poor" ) )
                        {
                            noMoneyLeft();
                        }
                        else if ( ( (String)message ).equals( "New Game" ) )
                        {
                            resetWindowSetting();
                        }
                        else if ( ( (String)message ).equals( "Not first decision" ) )
                        {
                            firstDecision = false;
                        }
                        else if ( ( (String)message ).equals( "Not enough money" ) )
                        {
                            enoughMoney = false;
                        }
                        else if ( ( (String)message ).equals( "everyone done" ) )
                        {
                            makingDecision = false;
                        }
                        else if ( ( (String)message ).equals( "First BJ" ) )
                        {
                            notBlackJack = false;
                        }
                        else if ( !isResultMessage( (String)message ) )
                        {
                            messageFromServer.setText( "SERVER: " + (String)message );
                        }
                    }
                    else if ( message instanceof Hand )
                    {
                        dealerHand = (Hand)message;
                        display.repaint();
                    }
                }
            } );
        }


        /**
         * This method is called when the hub shuts down. That is a signal that
         * the opposing player has quit the game. The user is informed of this,
         * and the program is terminated.
         */
        protected void serverShutdown( String message )
        {
            SwingUtilities.invokeLater( new Runnable()
            {
                public void run()
                {
                    JOptionPane.showMessageDialog( BJWindow.this, "Your opponent has quit.\nThe game is over." );
                    System.exit( 0 );
                }
            } );
        }

    }


    /**
     * The display class defines a JPanel that is used as the content pane for
     * the PokerWindow.
     */
    private class Display extends JPanel
    {

        final Color green = new Color( 0, 100, 0 );

        private JLabel IDText, moneyText;


        /**
         * The constructor creates labels, buttons, and a text field and adds
         * them to the panel. An action listener of type ButtonHandler is
         * created and is added to all the buttons and the text field.
         */
        Display()
        {
            setLayout( null ); // Layout will be done by hand.
            setPreferredSize( new Dimension( 700, 600 ) );
            setBackground( green );
            setResizable( false );
            createTexts();
            ButtonHandler listener = new ButtonHandler();
            hit = makeButton( "HIT", 555, 230, listener );
            stand = makeButton( "STAND", 555, 290, listener );
            doubleDown = makeButton( "DOUBLE", 555, 350, listener );
            surrender = makeButton( "SURRENDER", 555, 410, listener );
            quit = makeButton( "QUIT", 555, 500, listener );
            quit.setEnabled( true );
            firstDecision = true;
            enoughMoney = true;
            makingDecision = true;
            notBlackJack = true;
        }


        void createTexts()
        {
            total = makeLabel( 250, 525, 400, 30, 24, Color.WHITE );
            otherTotal1 = makeLabel( 75, 475, 400, 30, 24, Color.WHITE );
            otherTotal2 = makeLabel( 425, 475, 400, 30, 24, Color.WHITE );
            dealerTotal = makeLabel( 250, 175, 400, 30, 24, Color.WHITE );
            bet = makeLabel( 250, 350, 400, 30, 20, Color.WHITE );
            messageFromServer = makeLabel( 30, 280, 500, 25, 16, Color.WHITE );
            IDText = makeLabel( 545, 100, 400, 30, 20, Color.WHITE );
            IDText.setText( "You are" );
            moneyText = makeLabel( 545, 160, 400, 30, 20, Color.WHITE );
            moneyText.setText( "You have:" );
            ID = makeLabel( 560, 125, 400, 30, 30, Color.WHITE );
            money = makeLabel( 570, 185, 400, 30, 30, Color.WHITE );
            messageFromServer.setText( "SERVER: Choose Betting" );
        }


        /**
         * Utility routine used by constructor to make a label and add it to the
         * panel. The label has specified bounds, font size, and color, and its
         * text is initially empty.
         */
        JLabel makeLabel( int x, int y, int width, int height, int fontSize, Color color )
        {
            JLabel label = new JLabel();
            add( label );
            label.setBounds( x, y, width, height );
            label.setOpaque( false );
            label.setForeground( color );
            label.setFont( new Font( "Serif", Font.BOLD, fontSize ) );
            return label;
        }


        /**
         * Utility routine used by the constructor to make a button and add it
         * to the panel. The button has a specified text and (x,y) position and
         * is 80-by-35 pixels. An action listener is added to the button.
         */
        JButton makeButton( String text, int x, int y, ActionListener listener )
        {
            JButton button = new JButton( text );
            add( button );
            button.setEnabled( false );
            button.setBounds( x, y, 120, 50 );
            setFont( new Font( "SansSerif", Font.BOLD, 20 ) );
            button.addActionListener( listener );
            return button;
        }


        /**
         * The paint component just draws the cards, when appropriate. The
         * remaining content of the panel consists of sub-components (labels,
         * buttons, text field).
         */
        protected void paintComponent( Graphics g )
        {
            super.paintComponent( g );

            g.setColor( new Color( 82, 38, 0 ) );
            g.fillRect( 530, 0, 700, 600 );
            g.drawImage( gameLogo, 550, 10, 130, 90, this );
            if ( state == null )
            {
                return;
            }
            if ( state.hand == null )
            {
                return;
            }
            else
            {
                for ( int i = 0; i < playersHand.length; i++ )
                {
                    Card card = playersHand[i];
                    drawCard( g, card, ( i * 15 ) + 225, 400 - ( i * 10 ) );
                }
            }

            if ( state.allHands == null )
            {
                return;
            }
            else
            {
                int count = 0;
                int firstHand = 0;
                for ( int i = 0; i < 3; i++ )
                {
                    if ( i != playerID - 1 && othersHand[i] != null )
                    {
                        for ( int j = 0; j < othersHand[i].size(); j++ )
                        {
                            if ( firstHand == 0 )
                            {
                                firstHand = othersHand[i].size();
                            }
                            Card card = othersHand[i].get( j );
                            if ( count < firstHand )
                            {
                                drawCard( g, card, ( j * 15 ) + 50, 350 - ( j * 10 ) );
                                count++;
                                otherTotal1.setText( "" + othersHand[i].getTotal() );
                            }
                            else
                            {
                                drawCard( g, card, ( j * 15 ) + 400, 350 - ( j * 10 ) );
                                otherTotal2.setText( "" + othersHand[i].getTotal() );
                            }
                        }
                    }
                    else if ( othersHand[i] != null )
                    {
                        total.setText( "" + othersHand[i].getTotal() );
                    }
                }
            }

            if ( dealerHand == null )
            {
                return;
            }
            else if ( makingDecision )
            {
                drawCard( g, null, 225, 50 );
                for ( int i = 1; i < dealerHand.size(); i++ )
                {
                    Card card = dealerHand.get( i );
                    drawCard( g, card, ( i * 15 ) + 225, 50 - ( i * 10 ) );
                    dealerTotal.setText( "" + dealerHand.get( i ).getValue() );
                }
            }
            else
            {
                for ( int i = 0; i < dealerHand.size(); i++ )
                {
                    Card card = dealerHand.get( i );
                    drawCard( g, card, ( i * 15 ) + 225, 50 - ( i * 10 ) );
                    dealerTotal.setText( "" + dealerHand.getTotal() );
                }
            }
        }

    } // end nested class Display


    /**
     * A class to define the action listener that responds when the user clicks
     * a button or presses return while typing in the text field. Note that once
     * an action is taken, the buttons that were enabled are disabled, to
     * prevent the user from generating extra messages while the hub is
     * processing the user's action.
     */
    private class ButtonHandler implements ActionListener
    {
        public void actionPerformed( ActionEvent evt )
        {
            Object src = evt.getSource();
            if ( src == quit )
            { // end the program
                dispose();
                doQuit();
            }
            else if ( src == hit )
            {
                connection.send( "hit" );
                hit.setEnabled( false );
                stand.setEnabled( false );
                doubleDown.setEnabled( false );
                surrender.setEnabled( false );
            }
            else if ( src == stand )
            {
                connection.send( "stand" );
                hit.setEnabled( false );
                stand.setEnabled( false );
                doubleDown.setEnabled( false );
                surrender.setEnabled( false );
            }
            else if ( src == doubleDown )
            {
                connection.send( "double" );
                hit.setEnabled( false );
                stand.setEnabled( false );
                doubleDown.setEnabled( false );
                surrender.setEnabled( false );
            }
            else if ( src == surrender )
            {
                connection.send( "surrender" );
                hit.setEnabled( false );
                stand.setEnabled( false );
                doubleDown.setEnabled( false );
                surrender.setEnabled( false );
            }
        }
    } // end nested class ButtonHandler


    /**
     * This method is called when a new PokerGameState is received from the
     * networking. It changes the GUI and the window's state to match the new
     * game state. The new state is also stored in the instance variable named
     * state.
     */
    private void newState( GameState state )
    {

        this.state = state;

        hit.setEnabled( state.status == GameState.MAKE_DECISION && notBlackJack );
        stand.setEnabled( state.status == GameState.MAKE_DECISION );
        doubleDown
            .setEnabled( state.status == GameState.MAKE_DECISION && firstDecision && enoughMoney && notBlackJack );
        surrender.setEnabled( state.status == GameState.MAKE_DECISION && firstDecision && notBlackJack );

        moneyInt = state.money;
        money.setText( "$" + moneyInt );
        bettingInt = state.betting;
        bet.setText( "$" + bettingInt[playerID - 1] );
        playersHand = state.hand;
        othersHand = state.allHands;
    }


    private void noMoneyLeft()
    {
        JPanel panel = new JPanel();
        panel.setVisible( true );
        JLabel message = new JLabel( "You run out of MONEY!!" );
        panel.add( message );
        while ( true )
        {
            int result = JOptionPane.showConfirmDialog( this,
                panel,
                "Sorry",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE );

            if ( result == JOptionPane.OK_OPTION )
            {
                break;
            }
        }
        doQuit();
    }


    private boolean isResultMessage( String str )
    {
        if ( str.equals( "You win." ) )
        {
            winWindow();
            return true;
        }
        else if ( str.equals( "You lose." ) )
        {
            loseWindow();
            return true;
        }
        else if ( str.equals( "It's a push." ) )
        {
            pushWindow();
            return true;
        }
        return false;
    }


    private void winWindow()
    {
        JPanel panel = new JPanel();
        panel.setVisible( true );
        JLabel message = new JLabel( "You WIN!!" );
        panel.add( message );
        while ( true )
        {
            int result = JOptionPane.showConfirmDialog( this,
                panel,
                "RESULT",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE );

            if ( result == JOptionPane.OK_OPTION )
            {
                connection.send( "Done" );
                break;
            }
        }
    }


    private void loseWindow()
    {
        JPanel panel = new JPanel();
        panel.setVisible( true );
        JLabel message = new JLabel( "You LOSE..." );
        panel.add( message );
        while ( true )
        {
            int result = JOptionPane.showConfirmDialog( this,
                panel,
                "RESULT",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE );

            if ( result == JOptionPane.OK_OPTION )
            {
                connection.send( "Done" );
                break;
            }
        }
    }


    private void pushWindow()
    {
        JPanel panel = new JPanel();
        panel.setVisible( true );
        JLabel message = new JLabel( "It's PUSH." );
        panel.add( message );
        while ( true )
        {
            int result = JOptionPane.showConfirmDialog( this,
                panel,
                "RESULT",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE );

            if ( result == JOptionPane.OK_OPTION )
            {
                connection.send( "Done" );
                break;
            }
        }
    }


    private void resetWindowSetting()
    {
        state = null;
        bettingInt = null;
        dealerHand = null;
        playersHand = null;
        firstDecision = true;
        enoughMoney = true;
        makingDecision = true;
        notBlackJack = true;
        total.setText( "" );
        otherTotal1.setText( "" );
        otherTotal2.setText( "" );
        dealerTotal.setText( "" );
        bet.setText( "" );
        bettingWindow();
    }


    /**
     * This method is called when the user clicks the "QUIT" button or closed
     * the window. The client disconnects from the server before terminating the
     * program. This will be seen by the Hub, which will inform the other
     * player's program (if any), so that that program can also terminate.
     */
    private void doQuit()
    {
        dispose(); // Close the window.
        if ( connection != null )
        {
            connection.disconnect();
            try
            { // time for the disconnect message to be sent.
                Thread.sleep( 500 );
            }
            catch ( InterruptedException e )
            {
            }
        }
        System.exit( 0 );
    }


    private void bettingWindow()
    {
        JPanel panel = new JPanel();
        JPanel part = new JPanel();
        panel.setVisible( true );
        JLabel message = new JLabel( "Choose Betting" );
        panel.add( message );
        ButtonGroup group = new ButtonGroup();
        hundred = makeBetButton( part, "$100", group );
        twoHundred = makeBetButton( part, "$200", group );
        threeHundred = makeBetButton( part, "$300", group );
        fourHundred = makeBetButton( part, "$400", group );
        fiveHundred = makeBetButton( part, "$500", group );
        panel.add( part );
        while ( true )
        {
            int result = JOptionPane.showConfirmDialog( this,
                panel,
                "Choose Betting",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE );

            if ( result != JOptionPane.OK_OPTION )
            {
                return;
            }
            if ( hundred.isSelected() )
            {
                connection.send( new Integer( 100 ) );
                break;
            }
            else if ( twoHundred.isSelected() )
            {
                connection.send( new Integer( 200 ) );
                break;
            }
            else if ( threeHundred.isSelected() )
            {
                connection.send( new Integer( 300 ) );
                break;
            }
            else if ( fourHundred.isSelected() )
            {
                connection.send( new Integer( 400 ) );
                break;
            }
            else if ( fiveHundred.isSelected() )
            {
                connection.send( new Integer( 500 ) );
                break;
            }
            else
            {
                message.setText( "You didn't choose betting!" );
            }
        }
    }


    private JRadioButton makeBetButton( JPanel panel, String text, ButtonGroup group )
    {
        JRadioButton button = new JRadioButton( text );
        group.add( button );
        button.setEnabled( true );
        setFont( new Font( "SansSerif", Font.BOLD, 24 ) );
        panel.add( button );
        return button;
    }


    public void drawCard( Graphics g, Card card, int x, int y )
    {
        int cx;
        int cy;
        if ( card == null )
        {
            cy = 4 * 123; // coords for a face-down card.
            cx = 2 * 79;
        }
        else
        {
            cx = ( card.getNumber() - 1 ) * 79;
            cy = ( card.getSuit() - 1 ) * 123;
        }
        g.drawImage( cardImages, x, y, x + 79, y + 123, cx, cy, cx + 79, cy + 123, this );
    }

}