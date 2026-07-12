import { useEffect, useState } from 'react';

const DARK_KEY = 'darkMode';

export default function DarkModeToggle() {
  const [dark, setDark] = useState(() => localStorage.getItem(DARK_KEY) === 'true');

  useEffect(() => {
    document.body.classList.toggle('dark-mode', dark);
    localStorage.setItem(DARK_KEY, String(dark));
  }, [dark]);

  return (
    <div
      className="dark-mode-toggle"
      role="button"
      aria-label="Dark mode keçid"
      onClick={() => setDark(d => !d)}
    >
      <i className={dark ? 'fas fa-moon' : 'fas fa-sun'} />
    </div>
  );
}
