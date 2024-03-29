package hu.joti.betuparbaj.mgbeans;

import hu.joti.betuparbaj.model.Hit;
import hu.joti.betuparbaj.model.ScoringMode;
import hu.joti.betuparbaj.model.Word;
import hu.joti.betuparbaj.model.WordDao;
import hu.joti.betuparbaj.model.WordDaoTxt;
import hu.joti.betuparbaj.model.WordDaoMysql;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.Collections;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ApplicationScoped;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 *
 * @author Joti
 */
@ManagedBean(eager=true)
@ApplicationScoped
public class GlossaryManager implements Serializable{

  private static final Logger LOGGER = LogManager.getLogger(GlossaryManager.class.getName());

  private Set<String> words;
  private Map<String,String> easyVowelWords;
  
  /**
   * Creates a new instance of Glossary
   */
  public GlossaryManager() {
    
    words = new TreeSet<>();
    easyVowelWords = new TreeMap<>();
    System.out.println("GlossaryManager konstruktor");

    loadWords(false);
  }

  public final void loadWords(){
    loadWords(false);
  }

  public void loadWords(boolean forceTxt){
   
    WordDao wordDao;
    List<Word> wordList = null;
            
    if (!forceTxt){
      wordDao = new WordDaoMysql();
      wordList = wordDao.findAllWords();
      System.out.println(wordList == null);
    }  

    System.out.println(wordList == null);
    if (wordList == null){
      wordDao = new WordDaoTxt();
      wordList = wordDao.findAllWords();
    }

    if (!wordList.isEmpty()){
      String entry;
      String modEntry;

      words.clear();
      easyVowelWords.clear();

      for (Word word : wordList) {
        entry = word.getPhrase();
        entry = entry.replaceAll("1", "CS").replaceAll("2", "GY").replaceAll("3", "LY").replaceAll("4", "NY").replaceAll("5", "SZ").replaceAll("6", "TY").replaceAll("7", "ZS");       
        words.add(entry);

        if (entry.contains("Í") || entry.contains("Ó") || entry.contains("Ő") || entry.contains("Ú") || entry.contains("Ű")){
          modEntry = entry.replaceAll("Í", "I").replaceAll("Ó", "O").replaceAll("Ő", "Ö").replaceAll("Ú", "U").replaceAll("Ű", "Ü");
          if (!easyVowelWords.containsKey(modEntry))
            easyVowelWords.put(modEntry, entry);
        }
      }
      
      LOGGER.info("Alapszótár mérete: " + words.size());
      LOGGER.info("Hosszú mgh szótár mérete: " + easyVowelWords.size());
    } else {
      LOGGER.error("A szótár üres!");
    }
  }
  
  public boolean includes(String word, boolean easyVowelRule){
    return ( words.contains(word.toUpperCase()) || (easyVowelRule && easyVowelWords.containsKey(word.toUpperCase())) );
  }

  public String findGlossaryWord(String word, boolean easyVowelRule){
    String uWord = word.toUpperCase();
    if (words.contains(uWord))
      return uWord;

    if (easyVowelRule && easyVowelWords.containsKey(uWord))
        return easyVowelWords.get(uWord);
    
    return "";
  }

  public int saveWord(String word) {
    WordDaoMysql wordDaoMysql = new WordDaoMysql();
    int result = wordDaoMysql.saveWord(word);
    return result;
  }

  public int deleteWord(String word) {
    WordDaoMysql wordDaoMysql = new WordDaoMysql();
    int result = wordDaoMysql.deleteWord(word);
    return result;
  }
  
  public void saveWordsToDB(){
    /* szövegfájl tartalma alapján glossary adatbázis újraépítése */
    loadWords(true);

    List<Word> wordList = new ArrayList<>(words.size());
    
    for (String word : words) {
      Word w = new Word();
      w.setCategory(1);
      w.setPhrase(word);
      wordList.add(w);
    }
    
    Collections.sort(wordList);
    
    WordDaoMysql wordDaoMysql = new WordDaoMysql();
    wordDaoMysql.saveAllWords(wordList);
  } 

  public void saveWordsToFile(){
    /* glossary adatbázis mentése fájlba */
    loadWords();

    List<Word> wordList = new ArrayList<>(words.size());
    
    for (String word : words) {
      Word w = new Word();
      w.setCategory(1);
      w.setPhrase(word);
      wordList.add(w);
    }
    
    Collections.sort(wordList);
    
    WordDaoTxt wordDaoTxt = new WordDaoTxt();
    wordDaoTxt.saveAllWords(wordList);
  } 
  
  public Hit findHit(String[] letters, boolean easyVowelRule, ScoringMode scoringMode){
    String word;
    
    /* Az alábbi sorrendben ellenőrizzük a betűsorokat: 6 betűs: 1-6; 5 b.: 1-5, 2-6; 4 b.: 1-4, 2-5, 3-6; 3 b.: 1-3, 2-4, 3-5, 4-6; 2 b.: 1-2, 2-3, 3-4, 4-5, 5-6 */
    for (int len = letters.length; len > 1; len--) {
      for (int start = 0; start + len <= letters.length; start++) {
        word = "";
        for (int pos = start; pos - start < len ; pos++) {
          word += letters[pos];
        }
        
        String gWord = findGlossaryWord(word, easyVowelRule);
        if (!gWord.isEmpty()){
          Hit hit = new Hit(start, start + len - 1, scoringMode.getLenValues()[len - 1] , gWord);
          return hit;
        }
      }
    }
    return null;
  }
  
  public Set<String> getWords() {
    return words;
  }

  public void setWords(Set<String> words) {
    this.words = words;
  }

  public Map<String,String> getEasyVowelWords() {
    return easyVowelWords;
  }

  public void setEasyVowelWords(Map<String,String> easyVowelWords) {
    this.easyVowelWords = easyVowelWords;
  }

}
