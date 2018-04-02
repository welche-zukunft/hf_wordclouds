package welchezukunft;

import edu.stanford.nlp.util.ArrayUtils;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.logging.Redwood;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.ling.SentenceUtils;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;


/*	
ENG:
1.	CC	Coordinating conjunction
2.	CD	Cardinal number
3.	DT	Determiner
4.	EX	Existential there
5.	FW	Foreign word
6.	IN	Preposition or subordinating conjunction
7.	JJ	Adjective
8.	JJR	Adjective, comparative
9.	JJS	Adjective, superlative
10.	LS	List item marker
11.	MD	Modal
12.	NN	Noun, singular or mass
13.	NNS	Noun, plural
14.	NNP	Proper noun, singular
15.	NNPS	Proper noun, plural
16.	PDT	Predeterminer
17.	POS	Possessive ending
18.	PRP	Personal pronoun
19.	PRP$	Possessive pronoun
20.	RB	Adverb
21.	RBR	Adverb, comparative
22.	RBS	Adverb, superlative
23.	RP	Particle
24.	SYM	Symbol
25.	TO	to
26.	UH	Interjection
27.	VB	Verb, base form
28.	VBD	Verb, past tense
29.	VBG	Verb, gerund or present participle
30.	VBN	Verb, past participle
31.	VBP	Verb, non-3rd person singular present
32.	VBZ	Verb, 3rd person singular present
33.	WDT	Wh-determiner
34.	WP	Wh-pronoun
35.	WP$	Possessive wh-pronoun
36.	WRB	Wh-adverb

*/

public class Tagger  {

  /** A logger for this class */
  private static Redwood.RedwoodChannels log = Redwood.channels(Tagger.class);
  private MaxentTagger tagger;
  
  private String [] filterlist = {"NN","NNS","NNP","NNPS","FW"};
  
  public Tagger(String lang) {
	  String taggertext = "";
	  if(lang.equals("eng")){	 
		  taggertext = "english-bidirectional-distsim.tagger";
	  }
	  else if (lang.equals("de")){
		  taggertext = "german-fast.tagger";
	  }
	  tagger = new MaxentTagger("C:/Users/jitterhorse/workspace/max/lib/models/"+taggertext);
  }

  public void main(){
  }
  
  public List<String> getTags(String args){
	  String taggedString = this.tagger.tagString(args);
	  String [] words = taggedString.split(" ");
	  List<String> sorted = new ArrayList<String>();
	  for(String w : words){
		  String [] tag = w.split("_");
		  if(ArrayUtils.contains( filterlist, tag[1] ) == true){
			  sorted.add(tag[0]);
		  } 
	  }  
	  return sorted;
  }
  
  static void doSentenceTest(){
	    Properties props = new Properties();
	    props.put("annotators","tokenize, ssplit, pos, lemma, ner, parse, dcoref");
	    StanfordCoreNLP stanford = new StanfordCoreNLP(props);

	    TregexPattern npPattern = TregexPattern.compile("@NP");

	    String text = "The fitness room was dirty.";


	    // create an empty Annotation just with the given text
	    Annotation document = new Annotation(text);
	    // run all Annotators on this text
	    stanford.annotate(document);

	    List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
	    for (CoreMap sentence : sentences) {

	        Tree sentenceTree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
	        TregexMatcher matcher = npPattern.matcher(sentenceTree);

	        while (matcher.find()) {
	            //this tree should contain "The fitness room" 
	            Tree nounPhraseTree = matcher.getMatch();
	            //Question : how do I find that "dirty" has a relationship to the nounPhraseTree


	        }

	        // Output dependency tree
	        TreebankLanguagePack tlp = new PennTreebankLanguagePack();
	        GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
	        GrammaticalStructure gs = gsf.newGrammaticalStructure(sentenceTree);
	        //Collection<TypedDependency> tdl = gs.typedDependenciesCollapsed();

	        System.out.println("typedDependencies: "+gs); 

	    }

	}
  
  
  public void freePeer(){
	  tagger = null;
  }
 
 }
