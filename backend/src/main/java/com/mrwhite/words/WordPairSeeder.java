package com.mrwhite.words;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class WordPairSeeder implements CommandLineRunner {
    private final WordPairRepository repository;
    public WordPairSeeder(WordPairRepository repository) { this.repository = repository; }
    @Override public void run(String... args) {
        if (repository.count() > 0) return;
        String[][] pairs = {
            {"Apple","Pear"},{"Coffee","Tea"},{"Cat","Dog"},{"River","Lake"},{"Ocean","Sea"},{"Mountain","Hill"},
            {"Summer","Winter"},{"Rain","Snow"},{"Sun","Moon"},{"Star","Planet"},{"Book","Magazine"},{"Pen","Pencil"},
            {"Car","Bus"},{"Train","Tram"},{"Bicycle","Scooter"},{"Pizza","Pasta"},{"Bread","Toast"},{"Cake","Pie"},
            {"Chair","Sofa"},{"Table","Desk"},{"House","Apartment"},{"Garden","Park"},{"Tree","Flower"},{"Rose","Tulip"},
            {"Lion","Tiger"},{"Bear","Wolf"},{"Horse","Donkey"},{"Eagle","Hawk"},{"Shark","Dolphin"},{"Whale","Seal"},
            {"Guitar","Piano"},{"Drum","Violin"},{"Movie","Theatre"},{"Song","Music"},{"Game","Sport"},{"Ball","Glove"},
            {"Red","Orange"},{"Blue","Green"},{"Black","White"},{"Gold","Silver"},{"Happy","Joyful"},{"Sad","Lonely"},
            {"Fast","Quick"},{"Small","Tiny"},{"Big","Large"},{"Cold","Cool"},{"Hot","Warm"},{"Smart","Clever"},
            {"Doctor","Nurse"},{"Teacher","Student"},{"Chef","Baker"},{"Pilot","Driver"},{"King","Queen"},{"Prince","Princess"},
            {"Morning","Dawn"},{"Evening","Night"},{"Day","Week"},{"Monday","Friday"},{"Spring","Autumn"},{"Cloud","Sky"},
            {"Phone","Computer"},{"Camera","Video"},{"Internet","Website"},{"Email","Letter"},{"Key","Lock"},{"Clock","Watch"},
            {"Salt","Pepper"},{"Sugar","Honey"},{"Water","Juice"},{"Milk","Cream"},{"Apple","Fruit"},{"Carrot","Potato"},
            {"Beach","Island"},{"Forest","Jungle"},{"Desert","Canyon"},{"Road","Street"},{"Bridge","Tunnel"},{"Airport","Station"},
            {"Football","Basketball"},{"Tennis","Golf"},{"Swimming","Running"},{"Dance","Yoga"},{"Chess","Cards"},{"Dice","Puzzle"},
            {"Kingdom","Empire"},{"Castle","Palace"},{"Sword","Shield"},{"Hero","Villain"},{"Magic","Wizard"},{"Dragon","Monster"},
            {"Fire","Smoke"},{"Earth","Stone"},{"Wind","Breeze"},{"Light","Shadow"},{"Electricity","Energy"},{"Metal","Wood"},
            {"Family","Friend"},{"Love","Hate"},{"Laugh","Smile"},{"Cry","Tears"},{"Dream","Sleep"},{"Work","Job"},
            {"Market","Shop"},{"Money","Coin"},{"Gift","Present"},{"Party","Festival"},{"Holiday","Vacation"},{"Wedding","Birthday"}
        };
        repository.saveAll(List.of(pairs).stream().map(p -> new WordPair(p[0], p[1])).toList());
    }
}
