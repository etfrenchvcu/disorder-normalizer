// /*
//  * To change this template, choose Tools | Templates
//  * and open the template in the editor.
//  */
// package tool.sieves;

// import java.util.ArrayList;
// import java.util.List;

// import tool.util.HashListMap;
// import tool.util.Ling;
// import tool.util.Mention;
// import tool.util.Terminology;
// import tool.util.Util;

// /**
//  *
//  * @author
//  */
// public class StemmingSieve extends Sieve {
    
//     HashListMap stemmedNormalizedNameToCuiListMap;

//     /**
//      * Constructor. Calls abstract constructor.
//      * @param standardTerminology
//      * @param trainTerminology
//      */
//     public StemmingSieve(Terminology standardTerminology, Terminology trainTerminology) {
//         super(standardTerminology, trainTerminology);
//         stemmedNormalizedNameToCuiListMap = new HashListMap();
//     }

//     public String apply(Mention concept) {
//         transformName(concept);
//         return normalize(concept);
//     }     
    
//     private void transformName(Mention concept) {
//         List<String> namesForTransformation = new ArrayList<>(concept.getNamesKnowledgeBase());
//         List<String> transformedNames = new ArrayList<>();        
        
//         for (String nameForTransformation : namesForTransformation) {
//             transformedNames = Util.setList(transformedNames, Ling.getStemmedPhrase(nameForTransformation));
//         }
        
//         concept.setStemmedNamesKnowledgeBase(transformedNames);   
//     }    

//     public String normalize(Mention concept) {
//         for (String name : concept.getStemmedNamesKnowledgeBase()) {
//             String cui = exactStemmedMatchSieve(name);            
//             if (!cui.equals(""))
//                 return cui;
//         }
//         return "";
//     }    
    
//     public String exactStemmedMatchSieve(String name) {
//         String cui = "";
//         //checks against names normalized by multi-pass sieve
//         cui = getTerminologyNameCui(stemmedNormalizedNameToCuiListMap, name);
//         if (!cui.equals(""))
//             return cui;
        
//         //checks against names in training data
//         cui = getTerminologyNameCui(trainTerminology.stemmedNameToCuiListMap, name);
//         if (!cui.equals(""))
//             return cui;        
        
//         //checks against names in dictionary
//         cui = getTerminologyNameCui(standardTerminology.stemmedNameToCuiListMap, name);       
//         return cui;
//     }    
    
// }
