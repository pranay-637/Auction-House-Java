import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class AuctionSystem {
    public static void main(String[] args) {
        new MainFrame();
    }
}

//Data is here for items
class AuctionData {
    //Pehlu item by default
    public static String itemName = "Antique Pocket Watch";
    public static String itemDesc = "Rare 19th-century gold-plated watch.";
    public static int basePrice = 1000;
    
    public static int currentPrice = 1000;
    public static String winningUser = "None";
    public static int configuredTimerSeconds = 30;
    public static int timeRemaining = 30;
    public static boolean isAuctionActive = true;
    
    public static Timer auctionTimer;
    public static List<AuctionRoomFrame> activeRooms = new ArrayList<>();
    public static List<String> auctionHistory = new ArrayList<>();

    //Item nu auction aiya start karvanu
    public static synchronized void resetAndStartTimer() {
        if (auctionTimer != null) {
            auctionTimer.cancel();
        }

        timeRemaining = configuredTimerSeconds; //aa time ne reset kare bid thai tyare
        isAuctionActive = true;

        auctionTimer = new Timer(true);
        auctionTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (timeRemaining > 0) {
                    timeRemaining--;
                } else {
                    if (isAuctionActive) {
                        isAuctionActive = false;
                        String record = "Item: " + itemName + " | Winner: " + winningUser + " | Final Price: $" + currentPrice;
                        auctionHistory.add(record);
                    }
                    auctionTimer.cancel();
                    auctionTimer = null;
                }
                broadcastUpdate();
            }
        }, 1000, 1000);
    }

    public static void broadcastUpdate() {
        for (AuctionRoomFrame room : activeRooms) {
            room.updateDisplay();
        }
    }

    public static void resetAuctionForItem(String name, String desc, int base) {
        if (auctionTimer != null) {
            auctionTimer.cancel();
            auctionTimer = null;
        }
        itemName = name;
        itemDesc = desc;
        basePrice = base;
        currentPrice = base;
        winningUser = "None";
        timeRemaining = configuredTimerSeconds;
        isAuctionActive = true;
    }
}

// run kare ane aa frame aave
class MainFrame extends Frame implements ActionListener {
    Button btnAdmin, btnUsers, btnExit;

    public MainFrame() {
        setTitle("Auction System - Main Menu");
        setSize(400, 250);
        setLayout(new FlowLayout(FlowLayout.CENTER, 20, 30));

        btnAdmin = new Button("Admin");
        btnUsers = new Button("Users");
        btnExit = new Button("Exit");

        btnAdmin.setPreferredSize(new Dimension(100, 35));
        btnUsers.setPreferredSize(new Dimension(100, 35));
        btnExit.setPreferredSize(new Dimension(100, 35));

        btnAdmin.addActionListener(this);
        btnUsers.addActionListener(this);
        btnExit.addActionListener(this);

        add(btnAdmin);
        add(btnUsers);
        add(btnExit);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        setLocationRelativeTo(null);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnAdmin) {
            new AdminFrame();
        } else if (e.getSource() == btnUsers) {
            new UserFrame("User 1", 100, 150);
            new UserFrame("User 2", 550, 150);
            new UserFrame("User 3", 1000, 150);
        } else if (e.getSource() == btnExit) {
            System.exit(0);
        }
    }
}

// admin ni frame
class AdminFrame extends Frame implements ActionListener {
    TextField txtItemName, txtItemDesc, txtBasePrice, txtTimerSecs;
    Button btnSaveSettings, btnViewHistory, btnClose;
    TextArea txtHistoryDisplay;

    public AdminFrame() {
        setTitle("Admin Control Panel");
        setSize(480, 520);
        setLayout(new BorderLayout(10, 10));

        Panel formPanel = new Panel(new GridLayout(5, 2, 8, 8));

        formPanel.add(new Label("  Item Name:"));
        txtItemName = new TextField(AuctionData.itemName);
        formPanel.add(txtItemName);

        formPanel.add(new Label("  Item Description:"));
        txtItemDesc = new TextField(AuctionData.itemDesc);
        formPanel.add(txtItemDesc);

        formPanel.add(new Label("  Base Price ($):"));
        txtBasePrice = new TextField(String.valueOf(AuctionData.basePrice));
        formPanel.add(txtBasePrice);

        formPanel.add(new Label("  Timer (Seconds):"));
        txtTimerSecs = new TextField(String.valueOf(AuctionData.configuredTimerSeconds));
        formPanel.add(txtTimerSecs);

        btnSaveSettings = new Button("Set Item & Reset Timer");
        btnSaveSettings.addActionListener(this);
        formPanel.add(btnSaveSettings);

        btnViewHistory = new Button("Refresh Won Items List");
        btnViewHistory.addActionListener(this);
        formPanel.add(btnViewHistory);

        Panel centerPanel = new Panel(new BorderLayout(5, 5));
        centerPanel.add(new Label("Auction Winner History:"), BorderLayout.NORTH);
        
        txtHistoryDisplay = new TextArea(8, 40);
        txtHistoryDisplay.setEditable(false);
        centerPanel.add(txtHistoryDisplay, BorderLayout.CENTER);

        Panel bottomPanel = new Panel(new FlowLayout(FlowLayout.CENTER));
        btnClose = new Button("Close Admin Panel");
        btnClose.addActionListener(this);
        bottomPanel.add(btnClose);

        add(formPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        loadHistory();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void loadHistory() {
        txtHistoryDisplay.setText("");
        if (AuctionData.auctionHistory.isEmpty()) {
            txtHistoryDisplay.setText("No auctions completed yet.");
        } else {
            for (String log : AuctionData.auctionHistory) {
                txtHistoryDisplay.append(log + "
");
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnSaveSettings) {
            try {
                String name = txtItemName.getText().trim();
                String desc = txtItemDesc.getText().trim();
                int price = Integer.parseInt(txtBasePrice.getText().trim());
                int secs = Integer.parseInt(txtTimerSecs.getText().trim());

                AuctionData.configuredTimerSeconds = secs;
                AuctionData.resetAuctionForItem(name, desc, price);
                AuctionData.resetAndStartTimer();
                AuctionData.broadcastUpdate();

                txtHistoryDisplay.append("\n[Admin Updated Item: " + name + " | Timer: " + secs + "s]\n");
            } catch (NumberFormatException ex) {
                txtHistoryDisplay.setText("Error: Enter valid numeric values for price and timer.");
            }
        } else if (e.getSource() == btnViewHistory) {
            loadHistory();
        } else if (e.getSource() == btnClose) {
            dispose();
        }
    }
}

// user ni frame
class UserFrame extends Frame implements ActionListener {
    Button btnJoinAuction, btnExit;
    String userName;

    public UserFrame(String userName, int xPos, int yPos) {
        this.userName = userName;
        setTitle("Auction - " + userName);
        setSize(380, 200);
        setLocation(xPos, yPos);
        setLayout(new FlowLayout(FlowLayout.CENTER, 20, 30));

        btnJoinAuction = new Button("Join Auction");
        btnExit = new Button("Exit");

        btnJoinAuction.setPreferredSize(new Dimension(120, 40));
        btnExit.setPreferredSize(new Dimension(100, 40));

        btnJoinAuction.addActionListener(this);
        btnExit.addActionListener(this);

        add(btnJoinAuction);
        add(btnExit);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnJoinAuction) {
            new AuctionRoomFrame(userName);
        } else if (e.getSource() == btnExit) {
            dispose();
        }
    }
}

// aiya auction thai
class AuctionRoomFrame extends Frame implements ActionListener {
    Button btnBid100, btnBid500, btnBid1000, btnClose;
    Label lblItemTitle, lblItemDesc, lblBasePrice, lblCurrentPrice, lblHighestBidder, lblTimer;
    
    private String userName;

    public AuctionRoomFrame(String userName) {
        this.userName = userName;
        setTitle("Live Auction Room - " + userName);
        setSize(450, 400); 
        setLayout(new BorderLayout(10, 10));

        AuctionData.activeRooms.add(this);

        Panel detailsPanel = new Panel(new GridLayout(7, 1, 5, 5));
        
        lblItemTitle = new Label("Item: " + AuctionData.itemName, Label.CENTER);
        lblItemTitle.setFont(new Font("Arial", Font.BOLD, 16));

        lblItemDesc = new Label("Description: " + AuctionData.itemDesc, Label.CENTER);
        lblBasePrice = new Label("Base Price: $" + AuctionData.basePrice, Label.CENTER);
        
        lblTimer = new Label("Time Remaining: " + AuctionData.timeRemaining + "s", Label.CENTER);
        lblTimer.setFont(new Font("Arial", Font.BOLD, 14));
        lblTimer.setForeground(Color.RED);

        lblCurrentPrice = new Label("Current Price: $" + AuctionData.currentPrice, Label.CENTER);
        lblCurrentPrice.setFont(new Font("Arial", Font.BOLD, 14));
        lblCurrentPrice.setForeground(Color.BLUE);

        lblHighestBidder = new Label("Winning Bidder: " + AuctionData.winningUser, Label.CENTER);
        lblHighestBidder.setFont(new Font("Arial", Font.BOLD, 13));
        lblHighestBidder.setForeground(new Color(0, 120, 0));

        detailsPanel.add(lblItemTitle);
        detailsPanel.add(lblItemDesc);
        detailsPanel.add(lblBasePrice);
        detailsPanel.add(lblTimer);
        detailsPanel.add(lblCurrentPrice);
        detailsPanel.add(lblHighestBidder);

        Panel bidPanel = new Panel(new FlowLayout(FlowLayout.CENTER, 10, 15));
        
        btnBid100 = new Button("Bid +100");
        btnBid500 = new Button("Bid +500");
        btnBid1000 = new Button("Bid +1000");
        btnClose = new Button("Exit");

        btnBid100.addActionListener(this);
        btnBid500.addActionListener(this);
        btnBid1000.addActionListener(this);
        btnClose.addActionListener(this);

        bidPanel.add(btnBid100);
        bidPanel.add(btnBid500);
        bidPanel.add(btnBid1000);
        bidPanel.add(btnClose);

        add(detailsPanel, BorderLayout.CENTER);
        add(bidPanel, BorderLayout.SOUTH);

        // koi user join kare tyare aaj timer start thai
        AuctionData.resetAndStartTimer();
        updateDisplay();

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                AuctionData.activeRooms.remove(AuctionRoomFrame.this);
                dispose();
            }
        });

        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void updateDisplay() {
        lblItemTitle.setText("Item: " + AuctionData.itemName);
        lblItemDesc.setText("Description: " + AuctionData.itemDesc);
        lblBasePrice.setText("Base Price: $" + AuctionData.basePrice);
        lblCurrentPrice.setText("Current Price: $" + AuctionData.currentPrice);

        if (AuctionData.isAuctionActive) {
            lblTimer.setText("Time Remaining: " + AuctionData.timeRemaining + "s");
            lblTimer.setForeground(Color.RED);
            lblHighestBidder.setText("Winning Bidder: " + AuctionData.winningUser);
            
            btnBid100.setEnabled(true);
            btnBid500.setEnabled(true);
            btnBid1000.setEnabled(true);
        } else {
            lblTimer.setText("AUCTION CLOSED");
            lblTimer.setForeground(Color.GRAY);
            
            if (AuctionData.winningUser.equals("None")) {
                lblHighestBidder.setText("Auction ended with no bids.");
            } else {
                lblHighestBidder.setText("WINNER: " + AuctionData.winningUser + " at $" + AuctionData.currentPrice + "!");
            }

            btnBid100.setEnabled(false);
            btnBid500.setEnabled(false);
            btnBid1000.setEnabled(false);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!AuctionData.isAuctionActive && e.getSource() != btnClose) {
            return;
        }

        if (e.getSource() == btnBid100) {
            AuctionData.currentPrice += 100;
            AuctionData.winningUser = userName;
            AuctionData.resetAndStartTimer(); 
            AuctionData.broadcastUpdate();
        } else if (e.getSource() == btnBid500) {
            AuctionData.currentPrice += 500;
            AuctionData.winningUser = userName;
            AuctionData.resetAndStartTimer(); 
            AuctionData.broadcastUpdate();
        } else if (e.getSource() == btnBid1000) {
            AuctionData.currentPrice += 1000;
            AuctionData.winningUser = userName;
            AuctionData.resetAndStartTimer(); 
            AuctionData.broadcastUpdate();
        } else if (e.getSource() == btnClose) {
            AuctionData.activeRooms.remove(this);
            dispose();
        }
    }
}