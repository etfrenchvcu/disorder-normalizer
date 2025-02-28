// /*
//  * To change this template, choose Tools | Templates
//  * and open the template in the editor.
//  */
// package tool.sieves;

// import java.util.ArrayList;
// import java.util.List;

// import tool.util.Ling;
// import tool.util.Mention;
// import tool.util.Terminology;
// import tool.util.Util;

// /**
//  *
//  * @author
//  */
// public class SimpleNameSieve extends Sieve {

//     /**
//      * Constructor. Calls abstract constructor.
//      * 
//      * @param standardTerminology
//      * @param trainTerminology
//      */
//     public SimpleNameSieve(Terminology standardTerminology, Terminology trainTerminology) {
//         super(standardTerminology, trainTerminology);
//     }

//     public static String apply(Mention concept) {
//         List<String> namesForTransformation = getNamesForTransformation(concept);
//         List<String> namesKnowledgeBase = transformName(namesForTransformation);
//         String cui = Sieve.normalize(namesKnowledgeBase);
//         return cui.equals("") ? SimpleNameSieve.normalize(concept.getName()) : cui;
//     }

//     public static List<String> getNamesForTransformation(Mention concept) {
//         List<String> namesForTransformation = new ArrayList<>();
//         namesForTransformation.add(concept.getName());
//         if (!concept.getNameExpansion().equals(""))
//             namesForTransformation.add(concept.getNameExpansion());
//         return namesForTransformation;
//     }

//     private static List<String> transformName(List<String> namesForTransformation) {
//         List<String> transformedNames = new ArrayList<>();

//         for (String nameForTransformation : namesForTransformation) {
//             transformedNames = Util.addUnique(transformedNames,
//                     deletePhrasalModifier(nameForTransformation, nameForTransformation.split("\\s")));
//         }

//         return transformedNames;
//     }

//     public static List<String> deletePhrasalModifier(String phrase, String[] phraseTokens) {
//         List<String> newPhrases = new ArrayList<>();
//         if (phraseTokens.length > 3) {
//             String newPhrase = Ling.getSubstring(phraseTokens, 0, phraseTokens.length - 2) + " "
//                     + phraseTokens[phraseTokens.length - 1];
//             newPhrases = Util.setList(newPhrases, newPhrase);
//             newPhrase = Ling.getSubstring(phraseTokens, 1, phraseTokens.length);
//             newPhrases = Util.setList(newPhrases, newPhrase);
//         }
//         return newPhrases;
//     }

//     public static String normalize(String name) {
//         return Sieve.getTerminologyNameCui(Sieve.trainTerminology.simpleNameToCuiListMap, name);
//     }

// }
