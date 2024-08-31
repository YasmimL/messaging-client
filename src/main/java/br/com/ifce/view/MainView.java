package br.com.ifce.view;

import br.com.ifce.model.ChatMessage;
import br.com.ifce.model.Message;
import br.com.ifce.model.enums.MessageType;
import br.com.ifce.network.MessageListener;
import br.com.ifce.network.rmi.OfflineMessagingServiceProvider;
import br.com.ifce.network.socket.SocketClient;
import br.com.ifce.view.extensions.JPlaceholderTextField;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static br.com.ifce.util.ListUtil.castList;

public class MainView implements MessageListener {
    private final JFrame frame = new JFrame("Messaging Server");

    private final DefaultListModel<String> contactListModel;

    private JList<String> contactList;

    private JPanel messagesPanel;

    private final String username;

    private String selectedContact;

    private final Map<String, List<ChatMessage>> messages;

    public MainView(String username) {
        this.username = username;
        this.messages = new HashMap<>();
        this.contactListModel = new DefaultListModel<>();
    }

    private void onSelectContact() {
        this.selectedContact = this.contactList.getSelectedValue();
        this.clearChatPanel();
        if (this.messages.containsKey(this.selectedContact)) {
            this.messages.get(this.selectedContact).forEach(this::addChatMessage);
        }
    }

    public void show() throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        int frameWidth = 750;
        int frameHeight = 800;
        frame.setSize(frameWidth, frameHeight);
        this.frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JPanel contentPanel = new JPanel();
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        frame.setContentPane(contentPanel);

        this.renderHeadingPanel();
        this.renderMainPanel();

        this.frame.setVisible(true);
    }

    private void renderHeadingPanel() {
        JPanel panel = new JPanel();

        JLabel username = new JLabel("Hi, " + this.username + "!");
        username.setFont(new Font("Serif", Font.PLAIN, 24));
        panel.add(username, BorderLayout.WEST);

        panel.add(this.renderToggleStatusButton(), BorderLayout.EAST);

        this.frame.add(panel);
    }

    private JToggleButton renderToggleStatusButton() {
        final var online = "Online";
        final var offline = "Offline";

        JToggleButton toggleButton = new JToggleButton(online, true);
        toggleButton.addItemListener(e -> {
            final var isOnline = toggleButton.isSelected();
            toggleButton.setText(isOnline ? online : offline);

            final var messageType = isOnline ? MessageType.GO_ONLINE : MessageType.GO_OFFLINE;
            SocketClient.getInstance().send(messageType);

            if (isOnline) this.readOfflineMessages();
        });

        return toggleButton;
    }

    private void readOfflineMessages() {
        final var messages = OfflineMessagingServiceProvider.provide().readAll(this.username);
        if (!messages.isEmpty()) {
            messages.forEach(message -> {
                final var sender = message.from();
                if (!this.messages.containsKey(sender)) this.messages.put(sender, new ArrayList<>());
                this.messages.get(sender).add(message);
            });

            final var latestMessage = messages.get(messages.size() - 1);
            this.clearChatPanel();
            this.selectedContact = latestMessage.from();
            this.messages.get(latestMessage.from()).forEach(this::addChatMessage);
        }
    }

    private void renderMainPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));

        mainPanel.add(this.renderContactsPanel());
        mainPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        mainPanel.add(this.renderChatPanel());

        this.frame.add(mainPanel);
    }

    private JPanel renderContactsPanel() {
        final int width = 350;
        final int height = 400;

        JPanel contactsPanel = new JPanel(new BorderLayout());
        contactsPanel.setPreferredSize(new Dimension(width, height));

        JTextPane header = new JTextPane();
        header.setEditable(false);
        header.setBackground(new Color(1, 87, 155));
        header.setForeground(Color.WHITE);
        header.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
        header.setText("Contacts");
        StyledDocument doc = header.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);
        contactsPanel.add(header, BorderLayout.NORTH);

        this.contactList = new JList<>(this.contactListModel);
        this.contactList.setSelectionMode(0);
        this.contactList.setBounds(100, 100, 75, 75);
        this.contactList.setFixedCellHeight(30);
        this.contactList.addListSelectionListener((e) -> this.onSelectContact());

        JScrollPane scrollPane = new JScrollPane(this.contactList);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setPreferredSize(new Dimension(width, 200));
        contactsPanel.add(scrollPane, BorderLayout.CENTER);

        contactsPanel.add(this.renderActionsPanel(), BorderLayout.SOUTH);

        return contactsPanel;
    }

    private JPanel renderActionsPanel() {
        JPanel actionsPanel = new JPanel();
        actionsPanel.setLayout(new BoxLayout(actionsPanel, BoxLayout.X_AXIS));

        actionsPanel.add(this.renderAddContactButton());
        actionsPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        actionsPanel.add(this.renderRemoveContactButton());

        return actionsPanel;
    }

    private JButton renderAddContactButton() {
        JButton button = new JButton("Add Contact");
        button.addActionListener(e -> this.prepareShowAddContactsDialog());

        return button;
    }

    private void prepareShowAddContactsDialog() {
        SocketClient.getInstance().send(MessageType.GET_USERS_REQUEST);
    }

    private void showAddContactsDialog(List<String> contacts) {
        var contactsList = new JList<>(contacts.stream().filter(contact -> !this.contactListModel.contains(contact)).toArray());
        contactsList.setVisibleRowCount(5);
        contactsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        var topicsField = new JScrollPane(contactsList);
        var fields = new Object[]{
            "Select Contacts", topicsField,
        };

        var option = JOptionPane.showConfirmDialog(frame, fields, "Add Contacts", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option != JOptionPane.OK_OPTION) return;

        var selectedContacts = contactsList.getSelectedValuesList().stream().map(value -> (String) value).toList();
        this.contactListModel.addAll(selectedContacts);
        selectedContacts.forEach(contact -> this.messages.put(contact, new ArrayList<>()));
    }

    private JButton renderRemoveContactButton() {
        JButton button = new JButton("Remove Contact");
        button.addActionListener(e -> {
            if (this.contactList.getSelectedValue() == null) return;
            this.removeContact(this.contactList.getSelectedValue());
        });

        return button;
    }

    private void removeContact(String contact) {
        this.messages.remove(contact);
        this.contactListModel.removeElement(contact);
        this.selectedContact = null;
        this.clearChatPanel();
    }

    public JPanel renderChatPanel() {
        final int width = 350;
        final int height = 400;

        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.setPreferredSize(new Dimension(width, height));

        JTextPane header = new JTextPane();
        header.setEditable(false);
        header.setBackground(new Color(1, 87, 155));
        header.setForeground(Color.WHITE);
        header.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
        header.setText("Chat");
        StyledDocument doc = header.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);
        chatPanel.add(header, BorderLayout.NORTH);

        this.messagesPanel = new JPanel();
        this.messagesPanel.setLayout(new GridBagLayout());
        this.clearChatPanel();

        JScrollPane scrollPane = new JScrollPane(this.messagesPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setPreferredSize(new Dimension(width, 200));
        chatPanel.add(scrollPane, BorderLayout.CENTER);

        final int characterLimit = 27;
        JPanel formPanel = new JPanel(new BorderLayout());
        JTextField textField = new JPlaceholderTextField("Type your message...");
        textField.setPreferredSize(new Dimension(250, 25));
        textField.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent event) {
                if (textField.getText().length() >= characterLimit) event.consume();
            }
        });
        formPanel.add(textField, BorderLayout.WEST);

        JButton button = new JButton("Send");
        button.setPreferredSize(new Dimension(100, textField.getPreferredSize().height));
        button.addActionListener(event -> {
            if (this.selectedContact == null || textField.getText() == null || textField.getText().trim().isEmpty()) {
                return;
            }
            SocketClient.getInstance().send(
                new Message<>(
                    MessageType.CHAT_REQUEST,
                    new ChatMessage(
                        this.username,
                        this.selectedContact,
                        textField.getText(),
                        LocalTime.now()
                    )
                )
            );
            textField.setText("");
        });
        formPanel.add(button, BorderLayout.EAST);

        chatPanel.add(formPanel, BorderLayout.SOUTH);

        return chatPanel;
    }

    private void clearChatPanel() {
        this.messagesPanel.removeAll();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.SOUTH;
        constraints.weighty = Integer.MAX_VALUE;
        this.messagesPanel.add(new Label(), constraints);
        this.messagesPanel.repaint();
    }

    public void addChatMessage(ChatMessage message) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(129, 212, 250));

        JTextPane sender = new JTextPane();
        sender.setEditable(false);
        sender.setBackground(Color.BLACK);
        sender.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
        sender.setOpaque(false);
        sender.setText(this.username.equals(message.from()) ? "You" : message.from());
        header.add(sender, BorderLayout.WEST);

        JTextPane time = new JTextPane();
        time.setBackground(new Color(129, 212, 250));
        time.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
        time.setEditable(false);
        time.setText(message.time().format(DateTimeFormatter.ofPattern("HH:mm")));
        header.add(time, BorderLayout.EAST);

        panel.add(header, BorderLayout.NORTH);

        JTextPane body = new JTextPane();
        body.setEditable(false);
        body.setText(message.message());
        panel.add(body, BorderLayout.SOUTH);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.SOUTH;
        constraints.weightx = 1;
        constraints.weighty = 1;

        this.messagesPanel.add(panel, constraints);
        this.messagesPanel.validate();
        this.messagesPanel.repaint();

        SwingUtilities.invokeLater(() -> panel.scrollRectToVisible(panel.getBounds()));
    }

    @Override
    public void onMessage(Message<?> message) {
        switch (message.getType()) {
            case GET_USERS_RESPONSE -> this.showAddContactsDialog(castList((List<?>) message.getPayload()));
            case CHAT_RESPONSE -> this.handleChatMessage((ChatMessage) message.getPayload());
            case USER_LEFT -> this.handleUserLeave((String) message.getPayload());
        }
    }

    private void handleUserLeave(String userWhoLeft) {
        if (this.contactListModel.contains(userWhoLeft)) {
            this.removeContact(userWhoLeft);
        }
    }

    private void handleChatMessage(ChatMessage message) {
        final var contact = this.username.equals(message.to()) ? message.from() : message.to();

        if (!this.contactListModel.contains(contact)) this.contactListModel.addElement(contact);
        this.contactList.setSelectedValue(contact, true);
        this.selectedContact = contact;

        if (!this.messages.containsKey(contact)) this.messages.put(contact, new ArrayList<>());
        this.messages.get(contact).add(message);

        this.addChatMessage(message);
    }
}
