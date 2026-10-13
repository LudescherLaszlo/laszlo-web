package com.mrwhite.words;

import jakarta.persistence.*;

@Entity
@Table(name = "word_pair")
public class WordPair {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "word_a", nullable = false) private String wordA;
    @Column(name = "word_b", nullable = false) private String wordB;
    protected WordPair() {}
    public WordPair(String wordA, String wordB) { this.wordA = wordA; this.wordB = wordB; }
    public Long getId() { return id; }
    public String getWordA() { return wordA; }
    public String getWordB() { return wordB; }
}
