package org.eclipsetrader.core.feed;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

@RunWith(JUnitPlatform.class)
public class BookModernTest {

    @Test
    void testGetBidProposals() {
        IBookEntry[] bid = new IBookEntry[0];
        Book book = new Book(bid, null);
        Assertions.assertSame(bid, book.getBidProposals());
    }

    @Test
    void testGetAskProposals() {
        IBookEntry[] ask = new IBookEntry[0];
        Book book = new Book(null, ask);
        Assertions.assertSame(ask, book.getAskProposals());
    }

    @Test
    void testEqualsWithSameEntries() {
        IBookEntry[] bid = new IBookEntry[] { new BookEntry(null, 1.4, 100L, null, null) };
        IBookEntry[] ask = new IBookEntry[] { new BookEntry(null, 1.6, 100L, null, null) };
        Book book = new Book(bid, ask);
        Assertions.assertTrue(book.equals(new Book(bid, ask)));
    }

    @Test
    void testEqualsWithNewInstanceEntries() {
        Book book = new Book(new IBookEntry[] { new BookEntry(null, 1.4, 100L, null, null) },
                new IBookEntry[] { new BookEntry(null, 1.6, 100L, null, null) });
        Book newBook = new Book(new IBookEntry[] { new BookEntry(null, 1.4, 100L, null, null) },
                new IBookEntry[] { new BookEntry(null, 1.6, 100L, null, null) });
        Assertions.assertTrue(newBook.equals(book));
    }

    @Test
    void testNotEqualsBid() {
        Book book = new Book(new IBookEntry[] { new BookEntry(null, 1.4, 100L, null, null) },
                new IBookEntry[] { new BookEntry(null, 1.6, 100L, null, null) });
        Book newBook = new Book(new IBookEntry[] { new BookEntry(null, 1.41, 100L, null, null) },
                new IBookEntry[] { new BookEntry(null, 1.6, 100L, null, null) });
        Assertions.assertFalse(newBook.equals(book));
    }

    @Test
    void testNotEqualsAsk() {
        Book book = new Book(new IBookEntry[] { new BookEntry(null, 1.4, 100L, null, null) },
                new IBookEntry[] { new BookEntry(null, 1.6, 100L, null, null) });
        Book newBook = new Book(new IBookEntry[] { new BookEntry(null, 1.4, 100L, null, null) },
                new IBookEntry[] { new BookEntry(null, 1.61, 100L, null, null) });
        Assertions.assertFalse(newBook.equals(book));
    }

    @Test
    void testNotEqualsWithDifferentEntries() {
        Book book = new Book(new IBookEntry[] { new BookEntry(null, 1.4, 100L, null, null) },
                new IBookEntry[] { new BookEntry(null, 1.6, 100L, null, null) });
        Book newBook = new Book(new IBookEntry[] { new BookEntry(null, 1.4, 100L, null, null), new BookEntry(null, 1.41, 200L, null, null) },
                new IBookEntry[] { new BookEntry(null, 1.6, 100L, null, null) });
        Assertions.assertFalse(newBook.equals(book));
    }

    @Test
    void testNotEqualsWithNullBid() {
        Book book = new Book(new IBookEntry[] { new BookEntry(null, 1.4, 100L, null, null) },
                new IBookEntry[] { new BookEntry(null, 1.6, 100L, null, null) });
        Book newBook = new Book(null, new IBookEntry[] { new BookEntry(null, 1.6, 100L, null, null) });
        Assertions.assertFalse(newBook.equals(book));
    }

    @Test
    void testNotEqualsWithNullAsk() {
        Book book = new Book(new IBookEntry[] { new BookEntry(null, 1.4, 100L, null, null) },
                new IBookEntry[] { new BookEntry(null, 1.6, 100L, null, null) });
        Book newBook = new Book(new IBookEntry[] { new BookEntry(null, 1.4, 100L, null, null), new BookEntry(null, 1.41, 200L, null, null) }, null);
        Assertions.assertFalse(newBook.equals(book));
    }

    @Test
    void testEqualsWithOtherObjects() {
        Book book = new Book(null, null);
        Assertions.assertFalse(book.equals(new Object()));
    }

    @Test
    void testSerializable() throws Exception {
        Book book = new Book(new IBookEntry[] { new BookEntry(null, 1.4, 100L, null, null) },
                new IBookEntry[] { new BookEntry(null, 1.6, 100L, null, null) });
        ObjectOutputStream os = new ObjectOutputStream(new ByteArrayOutputStream());
        os.writeObject(book);
        os.close();
    }
}
