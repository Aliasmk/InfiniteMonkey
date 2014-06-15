

/*
 * Name: Aliasmk
 * Project: IMT
 * Description: This program generates and displaying random strings,
 * then finds and displays the English words in created amongst the jibberish. 
 * 
 *
 * The wordlist that accompanies is "2of12inf", open source and avaliable from 
 * http://sourceforge.net/projects/wordlist/
 *
 *
 * Version History
 * v 1.0
 * - Initial Release
 *
 * v 1.0.5
 * - Added Completion Sound
 *
 * v 1.1
 * - Replaced Util.Scanner with Buffered Reader. Speeds up by over 20 times.
 *
 * v 1.2
 * - Program now scans the dictionary into memory first,then searches it. Speeds up by over 20 times
 * - Added percentage completion to display when VERBOSEWORDS is turned off.
 * - Added a spinning wheel if the user does not want percentage.
 */

//imports
import java.io.*;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class IMT {

    //quick preferences
    final int PHRASES = 1000; //set maximum number of phrases. Varying this increases number of words and inherently, processing time.
    final int MAXWORDLETTERS = 7; //set maximum number of letters per "word"
    final int MINWORDLETTERS = 2; //set maximum number of letters per "word"
    
	final boolean PLAYSOUNDONFINISH = true; //play a sound on completion of processing
    
	final boolean VERBOSEARRAY = false; //specify whether or not the randomly generated array appears on the terminal screen. Recommended: false
    final boolean VERBOSEWORDS = false; //specify whether or not words are displayed on screen as they are discovered. Disabling shows a Percentage completion.
	final boolean NOPERCENT = true; //if true, when VERBOSEWORDS = false, the percentage will not be shown, instead, a spinning wheel will show to indicate activity.
	
	final boolean NEWMETHOD = true; //specifies whether to use the v1.1 or the v1.2 method of scanning.
	
	final String VERSION = "1.2"; //version number
    
    //global vars
    String[][] allWords;
	public String[] bufferedDict;
    String inputText = "";
    String checkLine = "";
    File dictionary;
    double timeSecondsEst;
    double wordsToScan = 0;
    Date datestart;
    BufferedReader lr;
	double percent = 0;
	int wordsScanned = 0;
	String confirmSound = "confirm2.s";
    
    //global objects
    DateFormat df = new SimpleDateFormat("dd-MM-yyyy_HH.mm.ss");
	DateFormat time = new SimpleDateFormat("HH:mm:ss");
    Random generator = new Random(); //create a random generator
    InputStreamReader isr = new InputStreamReader(System.in);
    BufferedReader br = new BufferedReader(isr);
    DecimalFormat decf = new DecimalFormat("###.##");

    //This method will initialize the directories needed for the logs and start the clock
    public void checkStartUp() throws Exception
	{
        System.out.println("Welcome to SE1 version " +VERSION +".");
		
		//attempt to find the dictionary file.
        //Filename is 2of12inf.dic and should be placed in the same directory 
        //as the compiled .class file!
		System.out.print("\nLoading dictionary...");
		dictionary = new File("./2of12inf.dic");

		System.out.print("\t\t\t\t [OK] \nSetting date...");
        datestart = new Date();
        
		System.out.print("\t\t\t\t\t [OK] \nVerifying log directories...");
        //here we make sure the postInfo directory exists, and if not, create it.
        File logDir = new File("./postInfo");
        if (!logDir.exists()) {
            
			System.out.print("\t [ERROR] \nMain log directory does not exist. Creating...");
			logDir.mkdir();  //make the dir if it doesn't exist
        }
		
		System.out.print("\t\t\t [OK] \nCreating results folder...");
        //now we create the folder that the log, array, and words file will reside in.
        File thisLog = new File("./postInfo/results-" + df.format(datestart));
        if (!thisLog.exists()) {
            System.out.print("\t\t\t [OK] \n");
			thisLog.mkdir();  //make the dir if it doesn't exist
        }
		
		System.out.print("Buffering Dictionary...");
		
		BufferedReader dbuf = new BufferedReader(new FileReader(dictionary));
		int dicLines = 0;
        //find number of lines
        while(dbuf.readLine() != null)
		{
            dicLines++;
        } 
		dbuf.close();
		BufferedReader dbuf2 = new BufferedReader(new FileReader(dictionary));
		bufferedDict = new String[dicLines+1];
		System.out.println("\t[" +dicLines +" words]");
		
		for(int i = 0; i < bufferedDict.length; i++)
		{
			bufferedDict[i] = dbuf2.readLine();
		}
		
		
		
		
    }
      
    //method to fill array with jibberish
    public void fillArray() {
        String letters = "aeiouyaeiouyaeiouybcdfghijklmnpqrstvwxyz";
        
        
        int numberOfRows = PHRASES;

        //set number of rows in array
        allWords = new String[numberOfRows][];
        System.out.print("Creating word array. This could take a while...");
		for (int row = 0; row < numberOfRows; row++) {
            //number of entries per row will be determined randomly
            //set maximum number of "words" in a phrase to be 30
            int rowLength = generator.nextInt(30) + 1;
            wordsToScan = wordsToScan + rowLength;

            //create a 1D array
            String[] phrase = new String[rowLength];

            for (int col = 0; col < rowLength; col++) {
                
                int wordLength = (generator.nextInt(MAXWORDLETTERS-MINWORDLETTERS+1) + MINWORDLETTERS);
                int firstChar = generator.nextInt(40);
                phrase[col] = String.valueOf(letters.charAt(firstChar));
                for (int character = 1; character < (wordLength); character++) {
                    int randomChar = generator.nextInt(40);
                    phrase[col] = (phrase[col]) + (String.valueOf(letters.charAt(randomChar)));
                }
            }

            allWords[row] = phrase;
        }
		System.out.print("\t [OK] \n");
    }

    //display array method
    public void displayArray() throws Exception {

		System.out.print("Creating array file...");
        //write the array contents to another file to keep the other logs clean
        File res = new File("./postInfo/results-" + df.format(datestart) + "/array.log");
        res.createNewFile();
        FileWriter reslogger = new FileWriter(res);

		System.out.print("\t\t\t\t [OK] \nWriting array. This could take a while...");
        //Write the actual original array to a log file, just to be able to compare results later. Can also be write to the terminal if VERBOSEARRAY = true;
        try {
            String punctuation = ".,!?";
            
            System.out.print("\t [OK] \n");                        
           
            
            for (int row = 0; row < allWords.length; row++) {
                for (int col = 0; col < allWords[row].length; col++) {
                    if(VERBOSEARRAY)
                    {
                        System.out.print(" " + allWords[row][col]);
                    }
                    
                    reslogger.write(" " + allWords[row][col]);
                }
                int randomPunctuation = generator.nextInt(4);
                
                if(VERBOSEARRAY)
                {
                    System.out.print(String.valueOf(punctuation.charAt(randomPunctuation)));
                    System.out.println();
                }
                
                
                reslogger.write(String.valueOf(punctuation.charAt(randomPunctuation)));
                reslogger.write("\n");
            }
        } catch (Exception e) {
            reslogger.write("An error was encountered while attempting to log the array.");
            reslogger.write("As follows: " + e);
			System.out.print("\t [ERROR] \nArray was not logged!");
        }
        reslogger.flush();
        reslogger.close();
    }
    
    //gets a psuedo-estimate time for completion. usually around correct.
    public void getEstimateTime() throws Exception {
        System.out.print("Estimating time to completion...");
        
        long expStart; 
		long expElap;
		expStart = System.nanoTime();
        checkWord("apple");
		checkWord("beta");
		checkWord("check");
		checkWord("delta");
		checkWord("foxtrot");
		checkWord("gamma");
		checkWord("hydro");
		checkWord("indigo");
		checkWord("jam");
		checkWord("kill");
		checkWord("lie");
		checkWord("mate");
		checkWord("net");
		checkWord("open");
		checkWord("pail");
		checkWord("queen");
		checkWord("rake");
		checkWord("sap");
		checkWord("take");
		checkWord("umbrella");
		checkWord("vantage");
		checkWord("walk");
		checkWord("xylophone");
		checkWord("yak");
		checkWord("zebra");
		checkWord("lulzthisisntaword");
        
        expElap = (System.nanoTime() - expStart)/27;
		
		
		//System.out.println(expElap +" nanoseconds per calculation. Also, calcd total words: " +wordsToScan);

        timeSecondsEst = ((wordsToScan * expElap)/1000000000);
		System.out.print("\t\t [OK] \n");
    }

    //method to find all English words in array
    public void logEnglishWords() throws Exception {
        System.out.print("Creating log and wordlist file...");
        //creating the results file that we store the number of words found and elapsed time and stuff.
        File results = new File("./postInfo/results-" + df.format(datestart) + "/results.log");
        results.createNewFile(); //create the log
        FileWriter resultsLogger = new FileWriter(results);
        resultsLogger.write("Log started at " +df.format(datestart) +"\n");

        //creating the worldlist file that we store and sort the words that were found. 
        File words = new File("./postInfo/results-" + df.format(datestart) + "/wordsfound.log");
        words.createNewFile(); //create the log
        FileWriter wordLogger = new FileWriter(words);
        wordLogger.write("--- Raw word list (Scroll for words of each length)--- \n");
		System.out.print("\t\t [OK] \nLoading completed.\n");
		System.out.println("\nUsing the following preferences:");
		System.out.println("\tPhrases: " +PHRASES);
		System.out.println("\tMinimum Word Length: " +MINWORDLETTERS);
		System.out.println("\tMaximum Word Length: " +MAXWORDLETTERS);
		if(!NEWMETHOD)
		{
			System.out.println("\tWarning! Using the v1.1 scanning method! Will be /very/ slow!");
		}
        //corresponds to maximum word length.
        double[] wordlength = new double[MAXWORDLETTERS+1];

        //here were intialize an array to contain all of the real words found. 
        //Because (there is a possibility!) that all words could be real, the 
        //array will contain wordsToScan items.
        String[] realWords = new String[(int) wordsToScan];

        //scanning of words
        double totalWords = 0;
		int inc = 0;
        
        System.out.println("\n*****************************************************************************");
        System.out.println("Finding any English words. Please be patient... \n" + "Estimated time to complete: " + Math.ceil(timeSecondsEst) + " seconds. (" + Math.round(timeSecondsEst / 60) + " mins or " + Math.round(timeSecondsEst / 60 / 60) + " hours.)");
		System.out.println("Start time: " +time.format(datestart));
        if(VERBOSEWORDS)
        {
            System.out.print("English words found so far: ");
        }
		else
		{
			if(!NOPERCENT)
				System.out.println("Percent Completion:    ");
		}
        
        long startTime = System.nanoTime(); //start timer
        
        for (int row = 0; row < allWords.length; row++) {
            for (int col = 0; col < allWords[row].length; col++) {
                wordsScanned++;
				
				 percent = (wordsScanned/wordsToScan)*100;
				 
				 if(!VERBOSEWORDS)
                 {
						if(!NOPERCENT)
						{
							System.out.print("\b\b\b" +(int)percent +"%");
						}
						else
						{	
							inc++;
							System.out.print("\b");
							if(inc == 1000/2)
							{
								System.out.print("|");
							}
							else if(inc == 2000/2)
							{
								System.out.print("/");
							}
							else if(inc == 3000/2)
							{
								System.out.print("-");
							}
							else if(inc == 4000/2)
							{
								System.out.print("\\");
							}
							else if(inc == 5000/2)
							{
								System.out.print("|");
							}
							else if(inc == 6000/2)
							{
								System.out.print("/");
							}
							else if(inc == 7000/2)
							{
								System.out.print("-");
							}
							else if(inc == 8000/2)
							{
								System.out.print("\\");
								inc = 0;
							}
						}
				 }
				
				if (this.checkWord(allWords[row][col])) {
                   
					
					//if a word in the array is real, great, print it out! if not, move on
                    if(VERBOSEWORDS)
                    {
                        System.out.print(allWords[row][col] + ", ");
                    }
					
                    
                    //TODO add a perentage 
                    
                    
                    
                    wordLogger.write(allWords[row][col] + "\n");

                    realWords[(int) totalWords] = allWords[row][col];

                    totalWords++;
                    
                    wordlength[allWords[row][col].toString().length()]++;
                }
            }
        }

        Date datefin = new Date();
        long elapsedTime = (System.nanoTime() - startTime) / 1000000000;

        wordLogger.flush();


        System.out.println("\nSearch complete. Found " + Math.round(totalWords) + " words.");
        System.out.println("See log files at \"" + results.getCanonicalPath() + "\" for more info.");
        System.out.println("*****************************************************************************");


        //Write the ending info in the logs
        resultsLogger.write("Dictionary file used: " + dictionary.getCanonicalPath() + "\n");
        resultsLogger.write("Scanned a total of " + Math.round(wordsToScan) + " possible words, " + Math.round(totalWords) + " of these were real. (" + decf.format((totalWords / wordsToScan) * 100) + "%) \n");
        resultsLogger.write("Percentage of word lengths as follows: \n");
        for (int n = MINWORDLETTERS; n <= MAXWORDLETTERS; n++) {
            resultsLogger.write("\t - " + n + " letters: " + Math.round(wordlength[n]) + " (" + decf.format((wordlength[n] / totalWords) * 100) + "%) \n");
        }
        resultsLogger.write("Finished on " + df.format(datefin) + "\n");
        resultsLogger.write("Elapsed time: " + elapsedTime + " seconds. (Estimated was " + Math.round(timeSecondsEst) + "s, " + Math.round(timeSecondsEst - elapsedTime) + "s off.)");
        resultsLogger.flush();
        resultsLogger.close();

        wordLogger.write("\n\n\n--- Words in order, also by length --- \n");

        for (int n = MINWORDLETTERS; n <= MAXWORDLETTERS; n++) {

            wordLogger.write("\n\n" + n + " letter words: \n");

            for (int i = 0; i < realWords.length; i++) {
                if (realWords[i] != null) {
                    if (realWords[i].toString().length() == n) {
                        //System.out.println(realWords[i]);
                        wordLogger.write("\t" + realWords[i] + "\n");
                    }
                }
            }
        }
        wordLogger.close();
    }
  
    //checking the word against the master dictionary
    public boolean checkWord(String input) throws Exception {
        
        //to hell with util.Scanner! Too slow!
		if(NEWMETHOD == true)
		{
	  
        boolean isWord = false;
        inputText = input.toLowerCase();
        
      
        
       for(int i=0; i < bufferedDict.length-1;i++)
	   {
		
			
				if(bufferedDict[i].equals(inputText))
				{
					isWord = true;
					break;
				}
			
		}
		return isWord;
		}
		else
		{
        
        
		
		boolean isWord = false;
        inputText = input.toLowerCase();
        
        BufferedReader lr2 = new BufferedReader(new FileReader(dictionary));
        checkLine = lr2.readLine();
        
        try {
            //while the word on the line that was just read does not equal the 
            //word we are looking for, go to the next line.
            while (!checkLine.equals(input)) {
                checkLine = lr2.readLine();
            }
            isWord = true;
        } catch (Exception e) {
            //if it reaches the end of the file it will throw an nullpointer error. 
            //thus if the error shows up, it will realize there are no lines left.

            //if by now it hasnt found the word, its not real. 
            isWord = false;
        } finally {
            //Close the scanner
            lr2.close();
        }
        
        return isWord;
		
		}
    }
    
    //master class
    public void masterClass() throws Exception {
        this.checkStartUp();
        this.fillArray();
        this.displayArray();
        this.getEstimateTime();
        this.logEnglishWords();
        
        
        if (PLAYSOUNDONFINISH) {
            AudioInputStream audioIn = AudioSystem.getAudioInputStream
                    (new File(confirmSound));
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
            Thread.sleep(4000);
        }
    }
    
    //main class
    public static void main(String[] args) throws Exception {
        IMT test = new IMT();
        test.masterClass();
    }
}