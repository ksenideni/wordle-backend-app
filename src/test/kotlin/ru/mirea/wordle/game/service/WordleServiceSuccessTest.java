package ru.mirea.wordle.game.service;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.mirea.wordle.game.exception.AlreadyWonException;
import ru.mirea.wordle.game.model.Color;
import ru.mirea.wordle.game.model.GuessResult;
import ru.mirea.wordle.game.model.Progress;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = {
                WordleServiceImpl.class
        }
)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class WordleServiceSuccessTest {
    @Autowired
    private WordleService wordleService;


    private static final String targetWord = "about";

    private static Progress progress = new Progress(
                false,
                List.of()
        );

    @Test
    @Order(0)
    public void makeFirstTry() {
        // given
        String conjecturalWord = "agony";
        // when
        progress = wordleService.makeTry(progress, conjecturalWord, targetWord);
        // then
        assertFalse(progress.getWon());

        List<GuessResult> tries = progress.getTries();
        assertEquals(1, tries.size());

        GuessResult guessResult = tries.get(0);
        List<GuessResult.Letter> letters = guessResult.getLetters();

        GuessResult.Letter aLetter = letters.get(0);
        assertEquals('a', aLetter.getCharacter());
        assertEquals(Color.GREEN, aLetter.getColor());

        GuessResult.Letter gLetter = letters.get(1);
        assertEquals('g', gLetter.getCharacter());
        assertEquals(Color.GREY, gLetter.getColor());

        GuessResult.Letter oLetter = letters.get(2);
        assertEquals('o', oLetter.getCharacter());
        assertEquals(Color.GREEN, oLetter.getColor());

        GuessResult.Letter nLetter = letters.get(3);
        assertEquals('n', nLetter.getCharacter());
        assertEquals(Color.GREY, nLetter.getColor());

        GuessResult.Letter yLetter = letters.get(4);
        assertEquals('y', yLetter.getCharacter());
        assertEquals(Color.GREY, yLetter.getColor());
    }

    @Test
    @Order(1)
    public void makeSecondTry() {
        // given
        String conjecturalWord = "abotu";
        // when
        progress = wordleService.makeTry(progress, conjecturalWord, targetWord);
        // then
        assertFalse(progress.getWon());

        List<GuessResult> tries = progress.getTries();
        assertEquals(2, tries.size());

        GuessResult guessResult = tries.get(1);
        List<GuessResult.Letter> letters = guessResult.getLetters();

        GuessResult.Letter aLetter = letters.get(0);
        assertEquals('a', aLetter.getCharacter());
        assertEquals(Color.GREEN, aLetter.getColor());

        GuessResult.Letter bLetter = letters.get(1);
        assertEquals('b', bLetter.getCharacter());
        assertEquals(Color.GREEN, bLetter.getColor());

        GuessResult.Letter oLetter = letters.get(2);
        assertEquals('o', oLetter.getCharacter());
        assertEquals(Color.GREEN, oLetter.getColor());

        GuessResult.Letter tLetter = letters.get(3);
        assertEquals('t', tLetter.getCharacter());
        assertEquals(Color.YELLOW, tLetter.getColor());

        GuessResult.Letter uLetter = letters.get(4);
        assertEquals('u', uLetter.getCharacter());
        assertEquals(Color.YELLOW, uLetter.getColor());
    }

    @Test
    @Order(2)
    public void makeThirdTry() {
        // given
        String conjecturalWord = "about";
        // when
        progress = wordleService.makeTry(progress, conjecturalWord, targetWord);
        // then
        assertTrue(progress.getWon());

        List<GuessResult> tries = progress.getTries();
        assertEquals(3, tries.size());

        GuessResult guessResult = tries.get(2);
        List<GuessResult.Letter> letters = guessResult.getLetters();

        GuessResult.Letter aLetter = letters.get(0);
        assertEquals('a', aLetter.getCharacter());
        assertEquals(Color.GREEN, aLetter.getColor());

        GuessResult.Letter bLetter = letters.get(1);
        assertEquals('b', bLetter.getCharacter());
        assertEquals(Color.GREEN, bLetter.getColor());

        GuessResult.Letter oLetter = letters.get(2);
        assertEquals('o', oLetter.getCharacter());
        assertEquals(Color.GREEN, oLetter.getColor());

        GuessResult.Letter uLetter = letters.get(3);
        assertEquals('u', uLetter.getCharacter());
        assertEquals(Color.GREEN, uLetter.getColor());

        GuessResult.Letter tLetter = letters.get(4);
        assertEquals('t', tLetter.getCharacter());
        assertEquals(Color.GREEN, tLetter.getColor());
    }

    @Test
    @Order(3)
    public void makeTryAfterWinning() {
        // when, then
        assertThrows(AlreadyWonException.class,
                () -> wordleService.makeTry(progress, targetWord, targetWord));
    }

}
