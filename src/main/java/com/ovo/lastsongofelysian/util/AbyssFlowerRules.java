package com.ovo.lastsongofelysian.util;

public final class AbyssFlowerRules {
    public static final int MAX_ENERGY = 300;
    public static final int REQUIRED_HITS = 20;
    public static final int CHANT_TIMEOUT = 1200;
    public static final int MIST_DURATION = 100;
    public static final int MIST_COOLDOWN = 300;
    public static final double MIST_RADIUS = 5.0D;
    public static final double CLEANSING_RADIUS = 5.0D;
    public static final double CHANT_RADIUS = 32.0D;
    public static final float MIST_DAMAGE = 5.0F;
    public static final float FIRST_DAMAGE = 5000.0F;
    public static final float FIRST_DRAIN = 200.0F;
    public static final int FIRST_DURATION = 100;

    public enum Power {
        ZEROTH(200, 12000,
                "我是来自深渊的黑，我是降自云霄的白，诞生自创世之前，存在于湮灭之后；我要将生命泉的水，白白赐给那口渴的人喝。神之键·黑渊白花，第零额定功率——圣枪的百岁兰！",
                "I am the black of the abyss, the white of the heavens, born before creation, enduring beyond oblivion; to the thirsty I freely give the water of life's spring. Divine Key: Abyss Flower, Zeroth Power—Holy Lance, the Hundred-Year Orchid!"),
        FIRST(0, 0,
                "我是吞噬一切的黑，我是孕育一切的白；诞生自创世之前，存在于湮灭之后；神之键·黑渊白花，第一额定功率——圣枪绽放！",
                "I am the black that devours all, the white that nurtures all; born before creation, enduring beyond oblivion. Divine Key: Abyss Flower, First Power—Holy Lance in Bloom!"),
        SECOND(0, 6000,
                "我是前往地狱的黑；我是来自天堂的白；神之键·黑渊白花，第二额定功率——圣枪逆流！",
                "I am the black bound for hell; I am the white from heaven. Divine Key: Abyss Flower, Second Power—Holy Lance, Reverse Flow!");

        public final int cost;
        public final int cooldown;
        public final String chant;
        public final String englishChant;

        Power(int cost, int cooldown, String chant, String englishChant) {
            this.cost = cost;
            this.cooldown = cooldown;
            this.chant = chant;
            this.englishChant = englishChant;
        }

        public boolean matches(String text) {
            return normalize(chant).equals(normalize(text)) || normalize(englishChant).equals(normalize(text));
        }
    }

    private AbyssFlowerRules() {
    }

    public static String normalize(String text) {
        StringBuilder result = new StringBuilder();
        text.codePoints().filter(Character::isLetterOrDigit).forEach(result::appendCodePoint);
        return result.toString().toLowerCase(java.util.Locale.ROOT);
    }

    public static float healingBudget(float casterHealth, float missingHealth) {
        return Math.max(0.0F, Math.min(missingHealth, casterHealth - 1.0F));
    }

    public static double remainingMaxHealth(double unrestrictedMax, double loss) {
        return Math.max(1.0D, unrestrictedMax - Math.max(0.0D, loss));
    }
}
