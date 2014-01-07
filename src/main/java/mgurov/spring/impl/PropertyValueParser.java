package mgurov.spring.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses the value to find placeholders and allow caller handle (resolve) them.
 */
public class PropertyValueParser {

    private final Pattern pattern;

    public PropertyValueParser() {
        this("${", "}");
    }

    public PropertyValueParser(String prefix, String suffix) {
        pattern = Pattern.compile(String.format("(%s(.*?)%s)", Pattern.quote(prefix), Pattern.quote(suffix)));
    }

    //TODO: non-String interface for performance?
    public static interface OnStringPartParsedEventListener {
        void onStart();

        void onResolvedStringPart(String part);

        /**
         * @param keyReference the bare key without prefix and suffix
         * @param placeholder original placeholder string with prefix and suffix, e.g. ${key}
         */
        void onPlaceholderPart(String keyReference, String placeholder);
        void onEnd();
    }

    /**
     * Notifies the listener about the parts of string parsed from the value
     * @return the listener passed
     */
    <T extends OnStringPartParsedEventListener> T parse(String value, T listener) {

        listener.onStart();

        final Matcher m = pattern.matcher(value);
        int unclaimedPosition = 0;
        while (m.find()) {
            if (m.start() > unclaimedPosition) {
                //TODO: char sequences?
                listener.onResolvedStringPart(value.substring(unclaimedPosition, m.start()));
            }
            unclaimedPosition = m.end();

            listener.onPlaceholderPart(m.group(2), m.group(1));
        }

        if (unclaimedPosition < value.length() - 1) {
            listener.onResolvedStringPart(value.substring(unclaimedPosition));
        }

        listener.onEnd();
        return listener;
    }
}
