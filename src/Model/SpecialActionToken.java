package Model;

public class SpecialActionToken {
    public boolean isSpade;
    public boolean isCultTack;
    public boolean isStrongholdAbility;
    public boolean isFactionAbility;

//BUnlar değiştirilip tekrardan false olarak set edilecek ama şimdilik böyle kalması lazım
    public SpecialActionToken() {
        this.isSpade = true;
        this.isCultTack = true;
        this.isStrongholdAbility = true;
        this.isFactionAbility = true;
    }
}
