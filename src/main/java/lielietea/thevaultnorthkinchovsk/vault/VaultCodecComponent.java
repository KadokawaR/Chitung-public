package lielietea.thevaultnorthkinchovsk.vault;

public interface VaultCodecComponent<U,V extends Valuable> {

    String userToJson(U user);

    U userFromJson(String jsonString);

    default String valuableToJson(V valuable){
        return valuable.tag();
    }

    V valuableFromJson(String jsonString);

}
