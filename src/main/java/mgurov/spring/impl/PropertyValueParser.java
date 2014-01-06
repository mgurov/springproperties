package mgurov.spring.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses the value to find placeholders and allow caller handle (resolve) them.
 */
public class PropertyValueParser {

    //TODO: configurable placeholder markers
    public static final Pattern PATTERN = Pattern.compile("\\$\\{(.*?)\\}");

    public static interface OnStringPartParsedEventListener {
        void onStart();
        void onResolvedStringPart(String part);
        //TODO: send the original entry I think
        void onPlaceholderPart(String placeholder);
        void onEnd();
    }

    /**
     * Notifies the listener about the parts of string parsed from the value
     * @return the listener passed
     */
    <T extends OnStringPartParsedEventListener> T parse(String value, T listener) {
        listener.onStart();

        Matcher m = PATTERN.matcher(value);
        if (!m.find()) {
            listener.onResolvedStringPart(value);
            listener.onEnd();
            return listener;
            //TODO: could we avoid this special case? I think so.
        }

        int unclaimedPosition = 0;
        do {
            if (m.start() > unclaimedPosition) {
                //TODO: char sequences?
                listener.onResolvedStringPart(value.substring(unclaimedPosition, m.start()));
            }
            unclaimedPosition = m.end();

            listener.onPlaceholderPart(m.group(1));
        } while (m.find());

        if (unclaimedPosition < value.length() - 1) {
            listener.onResolvedStringPart(value.substring(unclaimedPosition));
        }

        listener.onEnd();
        return listener;
    }
}
