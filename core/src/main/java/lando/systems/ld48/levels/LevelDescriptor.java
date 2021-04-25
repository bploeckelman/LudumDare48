package lando.systems.ld48.levels;

public enum LevelDescriptor {

      test("levels/test.tmx")
    , test2("levels/test2.tmx")
    ;

    public String mapFileName;

    LevelDescriptor(String mapFileName) {
        this.mapFileName = mapFileName;
    }

    @Override
    public String toString() {
        return "Level(" + mapFileName + ")";
    }

}
