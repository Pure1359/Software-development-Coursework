package testjava;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.*;

public class CardGame {

    // ------------------- Card -------------------
    static class Card {
        private final int value; // 1-12

        public Card(int value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

    // ------------------- Deck -------------------
    static class Deck {
        private final int deckPriority;
        private final ReentrantLock deckLock = new ReentrantLock();
        private final Condition notEmpty = deckLock.newCondition();
        private final ArrayDeque<Card> cardList = new ArrayDeque<>();

        public Deck(int deckPriority) {
            this.deckPriority = deckPriority;
        }

        public boolean tryLock(long timeoutMs) throws InterruptedException {
            return deckLock.tryLock(timeoutMs, TimeUnit.MILLISECONDS);
        }

        public void unlock() {
            deckLock.unlock();
        }

        public void addCard(Card card) {
            deckLock.lock();
            try {
                cardList.addLast(card);
                notEmpty.signalAll();
            } finally {
                deckLock.unlock();
            }
        }

        public Card withdrawCard() throws InterruptedException {
            deckLock.lock();
            try {
                while (cardList.isEmpty()) {
                    notEmpty.await();
                }
                return cardList.pollFirst();
            } finally {
                deckLock.unlock();
            }
        }

        public boolean isEmpty() {
            deckLock.lock();
            try {
                return cardList.isEmpty();
            } finally {
                deckLock.unlock();
            }
        }

        public ArrayDeque<Card> getCardList() {
            return cardList;
        }
    }

    // ------------------- Player -------------------
    static class Player implements Runnable {
        private final int playerIndex;
        private final Deck leftDeck;
        private final Deck rightDeck;
        private final List<Card> playerCards = new ArrayList<>();
        private final Random random = new Random();

        public Player(int playerIndex, Deck left, Deck right) {
            this.playerIndex = playerIndex;
            this.leftDeck = left;
            this.rightDeck = right;
        }

        public List<Card> getPlayerCards() {
            return playerCards;
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    if (leftDeck.tryLock(50)) {
                        try {
                            if (leftDeck.isEmpty()) continue;

                            if (rightDeck.tryLock(50)) {
                                try {
                                    if (rightDeck.isEmpty()) continue;

                                    // Withdraw one card from each deck
                                    Card c1 = leftDeck.withdrawCard();
                                    Card c2 = rightDeck.withdrawCard();
                                    playerCards.add(c1);
                                    playerCards.add(c2);
                                    System.out.println("Player " + playerIndex + " drew: " + c1 + ", " + c2);
                                } finally {
                                    rightDeck.unlock();
                                }
                            } else continue;
                        } finally {
                            leftDeck.unlock();
                        }
                    }

                    Thread.sleep(random.nextInt(20) + 10);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    // ------------------- Main -------------------
    public static void main(String[] args) throws InterruptedException {
        int nPlayers = 4; // number of players
        int nDecks = 2;   // number of decks
        int cardsPerDeck = 40;

        Random random = new Random();

        // Create decks
        List<Deck> decks = new ArrayList<>();
        for (int i = 0; i < nDecks; i++) {
            Deck deck = new Deck(i);
            for (int j = 0; j < cardsPerDeck / nDecks; j++) {
                deck.addCard(new Card(random.nextInt(12) + 1));
            }
            decks.add(deck);
        }

        // Create players with left/right decks (circular)
        List<Player> players = new ArrayList<>();
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < nPlayers; i++) {
            Deck left = decks.get(i % nDecks);
            Deck right = decks.get((i + 1) % nDecks);
            Player p = new Player(i, left, right);
            Thread t = new Thread(p);
            players.add(p);
            threads.add(t);
        }

        // Start players
        threads.forEach(Thread::start);

        // Let game run for a fixed time
        Thread.sleep(3000);

        // Stop players
        threads.forEach(Thread::interrupt);
        for (Thread t : threads) t.join();

        // Show results
        for (int i = 0; i < players.size(); i++) {
            System.out.println("Player " + i + " cards: " + players.get(i).getPlayerCards());
        }
        for (int i = 0; i < decks.size(); i++) {
            System.out.println("Deck " + i + " remaining: " + decks.get(i).getCardList());
        }
    }
}
