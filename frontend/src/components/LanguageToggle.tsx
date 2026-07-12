import { getLang, setLang, type Lang } from '../i18n/strings';

const LANGS: Lang[] = ['az', 'tr'];

export default function LanguageToggle() {
  const current = getLang();
  return (
    <div className="lang-toggle" role="group" aria-label="Dil seçimi">
      {LANGS.map(lang => (
        <button
          key={lang}
          className={current === lang ? 'active' : ''}
          onClick={() => {
            if (lang !== current) setLang(lang);
          }}
        >
          {lang.toUpperCase()}
        </button>
      ))}
    </div>
  );
}
