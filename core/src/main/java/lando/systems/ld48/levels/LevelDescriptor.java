package lando.systems.ld48.levels;

public enum LevelDescriptor {

      introduction("levels/introduction.tmx")
    , military("levels/military.tmx")
    , zuck_arena("levels/zuck-arena.tmx")
    , alien("levels/alien.tmx")
    , reptilian("levels/reptilian.tmx")
    , musk_arena("levels/musk-arena.tmx")
    , ending("")
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
