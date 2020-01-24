package org.owasp.esapi.reference;

import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Encoder;
import org.owasp.esapi.Logger;
import org.owasp.esapi.errors.IntrusionException;
import org.owasp.esapi.errors.ValidationException;
import org.owasp.esapi.reference.validation.HTMLValidationRule;

//package protected to prevent subclasses or extensions. Only for applying a Strategy pattern to account for issue 521
/*Package */class SafeHTMLValidationHandler {
    
    //Read property
    private static final String HTML_VALIDATION_PROPERTY = ESAPI.securityConfiguration().getStringProp(DefaultSecurityConfiguration.HTML_VALIDATION_MODE);
    //pull enum
    private static final HtmlValidationMode HTML_VALIDATION_CONFIGURATION = HtmlValidationMode.modeFor(HTML_VALIDATION_PROPERTY);
    
    //logger
    private static final Logger LOGGER = ESAPI.getLogger( SafeHTMLValidationHandler.class.getSimpleName() );
    
    //Two different support strategies
    enum HtmlValidationMode {
        //safe throws the exception.  Unsafe input fails.
        SAFE_HTML("safehtml"),
        //Clean overrides, catches the exception and logs it, returning a blank string.
        CLEAN_HTML("cleanhtml") {
            @Override
            public String getValidSafeHTML(HTMLValidationRule rule, String context, String input)
                    throws ValidationException, IntrusionException {
                String result = "";
                try {
                    result = super.getValidSafeHTML(rule, context, input);
                } catch (ValidationException ve) {
                    LOGGER.info(Logger.SECURITY_FAILURE, "Cleaned up invalit HTML input: " + ve );
                }
                return result;
            }
        };
        
        private final String id;
        private HtmlValidationMode(String modeId) {
            this.id = modeId;
        }
       
        
        public String getValidSafeHTML(HTMLValidationRule rule, String context, String input) throws ValidationException, IntrusionException {
            return rule.getValid(context, input);
        }
        
        // lookup defaults to clean (legacy support)
        public static HtmlValidationMode modeFor (String modeByName) {
            HtmlValidationMode mode = HtmlValidationMode.CLEAN_HTML;
            for (HtmlValidationMode valMod : HtmlValidationMode.values()) {
                if (valMod.id.equalsIgnoreCase(modeByName)) {
                    mode = valMod;
                    break;
                }
            }
            return mode;
        }
    }
 
    String getValidSafeHTML( String context, String input, Encoder encoder, int maxLength, boolean allowNull ) throws ValidationException, IntrusionException {
        HTMLValidationRule hvr = new HTMLValidationRule( HTML_VALIDATION_CONFIGURATION.id, encoder );
        hvr.setMaximumLength(maxLength);
        hvr.setAllowNull(allowNull);
        return HTML_VALIDATION_CONFIGURATION.getValidSafeHTML(hvr, context, input);
    }
    
}
