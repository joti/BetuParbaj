package hu.joti.betuparbaj.mgbeans;

import hu.joti.betuparbaj.model.Game;
import hu.joti.betuparbaj.model.Hit;
import hu.joti.betuparbaj.model.Word;
import hu.joti.betuparbaj.model.WordDao;
import hu.joti.betuparbaj.model.WordDaoTxt;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ApplicationScoped;
//import javax.enterprise.context.ApplicationScoped;

/**
 *
 * @author Joti
 */
@ManagedBean(eager=true)
@ApplicationScoped
public class Glossary implements Serializable{

  private Set<String> words;
  private Map<String,String> easyVowelWords;
  
  /**
   * Creates a new instance of Glossary
   * @throws java.io.FileNotFoundException
   * @throws java.io.IOException
   */
  public Glossary() throws FileNotFoundException, IOException {
    System.out.println("Glossary konstruktora indul");
    WordDao wordDao = new WordDaoTxt();
    
    words = new TreeSet<>();
    easyVowelWords = new TreeMap<>();

    List<Word> wordList = wordDao.findAllWords();
    String entry;
    String modEntry;
    
    for (Word word : wordList) {
      entry = word.getWord();
      entry = entry.replaceAll("1", "CS").replaceAll("2", "GY").replaceAll("3", "LY").replaceAll("4", "NY").replaceAll("5", "SZ").replaceAll("6", "TY").replaceAll("7", "ZS");       
      words.add(entry);
      
      if (entry.contains("Í") || entry.contains("Ó") || entry.contains("Ő") || entry.contains("Ú") || entry.contains("Ű")){
        modEntry = entry.replaceAll("Í", "I").replaceAll("Ó", "O").replaceAll("Ő", "Ö").replaceAll("Ú", "U").replaceAll("Ű", "Ü");
        easyVowelWords.put(modEntry, entry);
      }
    }

    System.out.println("Alapszótár mérete: " + words.size());
    System.out.println("Hosszú mgh. szótár mérete: " + easyVowelWords.size()); 
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
  
  public Hit findHit(String[] letters, boolean easyVowelRule, int scoringMode){
    String word;
    
    /* Az alábbi sorrendben ellenőrizzük a betűsorokat: 6 betűs: 1-6; 5 b.: 1-5, 2-6; 4 b.: 1-4, 2-5, 3-6; 3 b.: 1-3, 2-4, 3-5, 4-6; 2 b.: 1-2, 2-3, 3-4, 4-5, 5-6 */
    for (int len = letters.length; len > 1; len--) {
      System.out.println("vizsgált szóhossz: " + len);
      for (int start = 0; start + len <= letters.length; start++) {
        word = "";
        for (int pos = start; pos - start < len ; pos++) {
          word += letters[pos];
        }
        System.out.println("Vizsgált szó: " + word);
        
        String gWord = findGlossaryWord(word, easyVowelRule);
        if (!gWord.isEmpty()){
          System.out.println("Találat: " + gWord);
          Hit hit = new Hit(start, start + len - 1, Game.VALUE_OF_WORDS[scoringMode][len - 1] , gWord);
          System.out.println(hit.getWord() + " hossza: " + hit.getEnd() + " - " + hit.getStart() + " = " + (hit.getEnd() - hit.getStart()) + ", " + hit.getScore() + " pt");
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
