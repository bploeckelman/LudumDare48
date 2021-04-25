package lando.systems.ld48.levels;

public enum LevelDescriptor {

      introduction("levels/introduction.tmx")
    , test("levels/test.tmx")
    , test2("levels/test2.tmx")
    , test3("levels/test3.tmx")
    , reptilians("levels/reptilians.tmx")
    , core("levels/core.tmx")
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
