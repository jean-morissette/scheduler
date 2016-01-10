package au.id.ajlane.cron;

final class CronToken implements CharSequence {

    public static CronToken at(final int offset, final CharSequence characters) {
        return new CronToken(offset, characters);
    }

    public static CronToken of(final CharSequence characters) {
        return new CronToken(0, characters);
    }

    private final CharSequence characters;
    private final int offset;

    private CronToken(final int offset, final CharSequence characters) {
        this.offset = offset;
        this.characters = characters;
    }

    public int offset() { return offset; }

    @Override
    public int length() {
        return characters.length();
    }

    @Override
    public char charAt(final int index) {
        return characters.charAt(index);
    }

    @Override
    public CronToken subSequence(final int start, final int end) {
        return CronToken.at(offset + start, characters.subSequence(start, end));
    }

    public CronToken subSequence(final int start){
        return CronToken.at(offset + start, characters.subSequence(start, characters.length()));
    }

    public CronToken subSequenceOfLength(final int length){
        return CronToken.at(offset, characters.subSequence(0, length));
    }

    public CronToken subSequenceOfLength(final int start, final int length){
        return CronToken.at(offset + start, characters.subSequence(start, start + length));
    }

    @Override
    public String toString() {
        return characters.toString();
    }
}
