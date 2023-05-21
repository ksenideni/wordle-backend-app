package ru.mirea.wordle.game.service;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.mirea.wordle.game.exception.AlreadyLostException;
import ru.mirea.wordle.game.model.Color;
import ru.mirea.wordle.game.model.GuessResult;
import ru.mirea.wordle.game.model.Progress;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = {
                WordleServiceImpl.class
        }
)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WordleServiceFailTest {
    @Autowired
    private WordleService wordleService;

    private static final String targetWord = "bobar";

    private static Progress progress = new Progress(
            false,
            List.of()
    );

    @Test
    @Order(0)
    public void makeFirstTry() {
        // given
        String conjecturalWord = "bbqqq";
        // when
        progress = wordleService.makeTry(progress, conjecturalWord, targetWord);
        // then
        assertFalse(progress.getWon());

        List<GuessResult> tries = progress.getTries();
        assertEquals(1, tries.size());

        GuessResult guessResult = tries.get(0);
        List<GuessResult.Letter> letters = guessResult.getLetters();

        GuessResult.Letter b1Letter = letters.get(0);
        assertEquals('b', b1Letter.getCharacter());
        assertEquals(Color.GREEN, b1Letter.getColor());

        GuessResult.Letter b2Letter = letters.get(1);
        assertEquals('b', b2Letter.getCharacter());
        assertEquals(Color.YELLOW, b2Letter.getColor());

        GuessResult.Letter q1Letter = letters.get(2);
        assertEquals('q', q1Letter.getCharacter());
        assertEquals(Color.GREY, q1Letter.getColor());

        GuessResult.Letter q2Letter = letters.get(3);
        assertEquals('q', q2Letter.getCharacter());
        assertEquals(Color.GREY, q2Letter.getColor());

        GuessResult.Letter q3Letter = letters.get(4);
        assertEquals('q', q3Letter.getCharacter());
        assertEquals(Color.GREY, q3Letter.getColor());
    }

    @Test
    @Order(1)
    public void makeSecondTry() {
        // given
        String conjecturalWord = "bqqbb";
        // when
        progress = wordleService.makeTry(progress, conjecturalWord, targetWord);
        // then
        assertFalse(progress.getWon());

        List<GuessResult> tries = progress.getTries();
        assertEquals(2, tries.size());

        GuessResult guessResult = tries.get(1);
        List<GuessResult.Letter> letters = guessResult.getLetters();

        GuessResult.Letter b1Letter = letters.get(0);
        assertEquals('b', b1Letter.getCharacter());
        assertEquals(Color.GREEN, b1Letter.getColor());

        GuessResult.Letter q1Letter = letters.get(1);
        assertEquals('q', q1Letter.getCharacter());
        assertEquals(Color.GREY, q1Letter.getColor());

        GuessResult.Letter q2Letter = letters.get(2);
        assertEquals('q', q2Letter.getCharacter());
        assertEquals(Color.GREY, q2Letter.getColor());

        GuessResult.Letter b2Letter = letters.get(3);
        assertEquals('b', b2Letter.getCharacter());
        assertEquals(Color.YELLOW, b2Letter.getColor());

        GuessResult.Letter b3Letter = letters.get(4);
        assertEquals('b', b3Letter.getCharacter());
        assertEquals(Color.GREY, b3Letter.getColor());
    }

    @Test
    @Order(2)
    public void makeThirdTry() {
        // given
        String conjecturalWord = "qqqra";
        // when
        progress = wordleService.makeTry(progress, conjecturalWord, targetWord);
        // then
        assertFalse(progress.getWon());

        List<GuessResult> tries = progress.getTries();
        assertEquals(3, tries.size());

        GuessResult guessResult = tries.get(2);
        List<GuessResult.Letter> letters = guessResult.getLetters();

        GuessResult.Letter letter1 = letters.get(0);
        assertEquals('q', letter1.getCharacter());
        assertEquals(Color.GREY, letter1.getColor());

        GuessResult.Letter letter2 = letters.get(1);
        assertEquals('q', letter2.getCharacter());
        assertEquals(Color.GREY, letter2.getColor());

        GuessResult.Letter letter3 = letters.get(2);
        assertEquals('q', letter3.getCharacter());
        assertEquals(Color.GREY, letter3.getColor());

        GuessResult.Letter letter4 = letters.get(3);
        assertEquals('r', letter4.getCharacter());
        assertEquals(Color.YELLOW, letter4.getColor());

        GuessResult.Letter letter5 = letters.get(4);
        assertEquals('a', letter5.getCharacter());
        assertEquals(Color.YELLOW, letter5.getColor());
    }


    @Test
    @Order(3)
    public void makeFourthTry() {
        // given
        String conjecturalWord = "bobaa";
        // when
        progress = wordleService.makeTry(progress, conjecturalWord, targetWord);
        // then
        assertFalse(progress.getWon());

        List<GuessResult> tries = progress.getTries();
        assertEquals(4, tries.size());

        GuessResult guessResult = tries.get(3);
        List<GuessResult.Letter> letters = guessResult.getLetters();

        GuessResult.Letter letter1 = letters.get(0);
        assertEquals('b', letter1.getCharacter());
        assertEquals(Color.GREEN, letter1.getColor());

        GuessResult.Letter letter2 = letters.get(1);
        assertEquals('o', letter2.getCharacter());
        assertEquals(Color.GREEN, letter2.getColor());

        GuessResult.Letter letter3 = letters.get(2);
        assertEquals('b', letter3.getCharacter());
        assertEquals(Color.GREEN, letter3.getColor());

        GuessResult.Letter letter4 = letters.get(3);
        assertEquals('a', letter4.getCharacter());
        assertEquals(Color.GREEN, letter4.getColor());

        GuessResult.Letter letter5 = letters.get(4);
        assertEquals('a', letter5.getCharacter());
        assertEquals(Color.GREY, letter5.getColor());
    }

    @Test
    @Order(4)
    public void makeFifthTry() {
        // given
        String conjecturalWord = "aaaaa";
        // when
        progress = wordleService.makeTry(progress, conjecturalWord, targetWord);
        // then
        assertFalse(progress.getWon());

        List<GuessResult> tries = progress.getTries();
        assertEquals(5, tries.size());

        GuessResult guessResult = tries.get(4);
        List<GuessResult.Letter> letters = guessResult.getLetters();

        GuessResult.Letter letter1 = letters.get(0);
        assertEquals('a', letter1.getCharacter());
        assertEquals(Color.GREY, letter1.getColor());

        GuessResult.Letter letter2 = letters.get(1);
        assertEquals('a', letter2.getCharacter());
        assertEquals(Color.GREY, letter2.getColor());

        GuessResult.Letter letter3 = letters.get(2);
        assertEquals('a', letter3.getCharacter());
        assertEquals(Color.GREY, letter3.getColor());

        GuessResult.Letter letter4 = letters.get(3);
        assertEquals('a', letter4.getCharacter());
        assertEquals(Color.GREEN, letter4.getColor());

        GuessResult.Letter letter5 = letters.get(4);
        assertEquals('a', letter5.getCharacter());
        assertEquals(Color.GREY, letter5.getColor());
    }

    @Test
    @Order(5)
    public void makeTryAfterLosing() {
        // given
        String conjecturalWord = "aaaaa";
        // when, then
        assertThrows(AlreadyLostException.class,
                () -> wordleService.makeTry(progress, conjecturalWord, targetWord));
    }

}
