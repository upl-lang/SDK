package upl.regex3;

import upl.regex3.automaton.DFA;
import upl.regex3.automaton.DFAGraphics;
import upl.regex3.automaton.DFAState;
import upl.regex3.lang.BasicOperations;
import upl.regex3.syntaxtree.SyntaxTree;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Application {
    public static void main(String[] args) throws IOException {
        System.out.println("---SIMPLE REGEX LIBRARY TESTING PROGRAM---");
        int choiceA = 0, choiceB, choiceC;
        String str;
        boolean err0 = false;
        BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
        while (choiceA != 4 && !err0) {
            System.out.println(
                    "1.Interact with some Regex (RE->DFA->minDFA,Complement|Totalization,etc)\n" +
                            "2.Check diffOperation (a1Lang\\a2Lang).\n" +
                            "3.Check Intersection DFA a1 and DFA a2\n" +
                            "4.EXIT");
            choiceA = Integer.parseInt(console.readLine());
            switch (choiceA) {
                case 1: {
                    System.out.println("Enter the regEx in string format:-->");
                    str = console.readLine();
                    boolean err = false;
                    do {
                        System.out.println(
                                "1.Generate Syntax Tree\n" +
                                        "2.Generate DFA transitions table\n" +
                                        "3.Check String\n" +
                                        "4.Generate DFA State Diagram (Graphics)\n" +
                                        "5.Generate minDFA\n" +
                                        "6.Get Complement|Totalization of this Lang\n" +
                                        "7.Exit"
                        );
                        choiceB = Integer.parseInt(console.readLine());
                        switch (choiceB) {
                            case 1: {
                                SyntaxTree sTreeObj = new SyntaxTree(str);
                                sTreeObj.printData();
                                System.out.println();
                                sTreeObj.print();
                                break;
                            }
                            case 2: {
                                DFA automaton = new DFA(str);
                                automaton.printTable();
                                break;
                            }
                            case 3: {
                                String str2;
                                System.out.println("Enter the string for checking:");
                                str2 = console.readLine();
                                DFA automaton = new DFA(str);
                                automaton.isValidString(str2);
                                break;
                            }
                            case 4: {
                                DFA automaton = new DFA(str);
                                new DFAGraphics(automaton, "Generated DFA (not minimized) Regex:\"" + str + "\"");
                                break;
                            }
                            case 5: {
                                DFAState.resetIds();
                                DFA automaton = new DFA(str);
                                DFA minDFA = automaton.minimize();
                                new DFAGraphics(minDFA, "Generated DFA (minimized) Regex:\"" + str + "\"");
                                break;
                            }
                            case 6: {
                                System.out.println("Введите Universum в формате %c....%c... (все входящие символы подряд)");
                                List<String> universumChars = List.of(console.readLine().split(""));
                                Set<String> universumSet = new HashSet<>(universumChars);
                                boolean err_ = false;
                                do {
                                    System.out.println("1.Вывести Дополнение\n" +
                                            "2.Вывести Минимальное дополнение\n" +
                                            "3.Вывести Тотализированный Язык(автомат)\n" +
                                            "4.Ввести другой универсум\n" +
                                            "5.Выйти.");
                                    choiceC = Integer.parseInt(console.readLine());
                                    switch (choiceC) {
                                        case 1: {
                                            DFAState.resetIds();
                                            DFA automaton = new DFA(str);
                                            DFA dfaComplement = automaton.getComplement(universumSet);
                                            new DFAGraphics(dfaComplement, universumSet, "Complement 1 (not minimized) Regex:\"" + str + "\"");
                                            break;
                                        }
                                        case 2: {
                                            DFAState.resetIds();
                                            DFA automaton = new DFA(str);
                                            DFA minDFAComplement = automaton.getComplement(universumSet).minimize();
                                            new DFAGraphics(minDFAComplement, universumSet, "Coplement 1 (minimized) Regex:\"" + str + "\"");
                                            break;
                                        }
                                        case 3: {
                                            DFAState.resetIds();
                                            DFA automaton = new DFA(str);
                                            DFA dfaTotal = automaton.getTotal(universumSet);
                                            new DFAGraphics(dfaTotal, "DFA Totalized Regex:\"" + str + "\"" + " Universum: " + universumSet);
                                            break;
                                        }
                                        case 4: {
                                            System.out.println("Введите Universum в формате %c....%c... (все входящие символы подряд)");
                                            universumChars = List.of(console.readLine().split(""));
                                            universumSet = new HashSet<>(universumChars);
                                            break;
                                        }
                                        case 5: {
                                            System.out.println("----BACK TO MAIN MENU----");
                                            break;
                                        }
                                        default: {
                                            err_ = true;
                                            System.out.println("----INVALID INPUT ----");
                                            System.out.println("----BACK TO MAIN MENU----");
                                            break;
                                        }
                                    }
                                } while (choiceC != 5 && !err_);
                                break;
                            }
                            case 7: {
                                System.out.println("----BACK TO START MENU----");
                                break;
                            }
                            default: {
                                err = true;
                                System.out.println("----INVALID INPUT ----");
                                System.out.println("----BACK TO START MENU----");
                                break;
                            }
                        }
                    } while (choiceB != 7 && !err);
                    break;
                }
                case 2: {
                    boolean err = false;
                    do {
                        System.out.println("1.Ввести новые два языка.\n" +
                                "2.Выйти.");
                        choiceC = Integer.parseInt(console.readLine());
                        switch (choiceC) {
                            case 1: {
                                System.out.println("Введите регулярные выражения для обоих языков: ");
                                System.out.println("Regex(a1): ");
                                String s1 = console.readLine(); //"(a|b|d)*c"
                                System.out.println("Regex(a2): ");
                                String s2 = console.readLine(); //"baac*"
                                DFAState.resetIds();
                                DFA a1 = new DFA(s1);
                                new DFAGraphics(a1.minimize(), "DFA_1 (a1 Minimized) Regex:\"" + s1 + "\"");
                                DFA a2 = new DFA(s2);
                                DFAState.resetIds();
                                new DFAGraphics(a2.minimize(), "DFA_2 (a2 Minimized) Regex:\"" + s2 + "\"");
                                DFA c = BasicOperations.minus(a1, a2);
                                new DFAGraphics(c, "DFA_3 (a1\\a2) a1: \"" + s1 + "\" a2: \"" + s2 + "\" (minimized)");
                                break;
                            }
                            case 2: {
                                System.out.println("----BACK TO START MENU----");
                                break;
                            }
                            default: {
                                err = true;
                                System.out.println("----INVALID INPUT ----");
                                System.out.println("----BACK TO START MENU----");
                                break;
                            }
                        }
                    } while (choiceC != 2 && !err);
                    break;
                }
                case 3: {
                    boolean err = false;
                    do {
                        System.out.println("1.Ввести новые два языка.\n" +
                                "2.Выйти.");
                        choiceC = Integer.parseInt(console.readLine());

                        switch (choiceC) {
                            case 1: {
                                System.out.println("Введите регулярные выражения для обоих языков: ");
                                System.out.println("Regex(a1): ");
                                String s1 = console.readLine(); //"(b*|(b*ab*ab*)*)"
                                System.out.println("Regex(a2): ");
                                String s2 = console.readLine(); //"(b*|(b*abb*)*)"

                                DFAState.resetIds();
                                DFA a1 = new DFA(s1);
                                new DFAGraphics(a1.minimize(), "DFA 1 Minimized");
                                DFA a2 = new DFA(s2);
                                DFAState.resetIds();
                                new DFAGraphics(a2.minimize(), "DFA 2 Minimized");
                                DFA c = BasicOperations.intersection(a1, a2);
                                new DFAGraphics(c, "INTERSECTION Regex1: " + s1 + " Regex2: " + s2);
                                break;
                            }
                            case 2: {
                                System.out.println("----BACK TO START MENU----");
                                break;
                            }
                            default: {
                                err = true;
                                System.out.println("----INVALID INPUT ----");
                                System.out.println("----BACK TO START MENU----");
                                break;
                            }
                        }
                    } while (choiceC != 2 && !err);
                    break;
                }
                case 4: {
                    System.out.println("EXITING THE PROGRAM ...");
                    break;
                }
                default: {
                    err0 = true;
                    System.out.println("----INVALID INPUT ----");
                    System.out.println("EXITING THE PROGRAM ...");
                    break;
                }
            }
        }
    }
}
