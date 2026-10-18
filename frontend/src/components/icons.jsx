// Lightweight stroke-icon set (no external dependency).
// Every icon accepts standard SVG props (size, className, etc).
const base = {
  viewBox: '0 0 24 24',
  fill: 'none',
  stroke: 'currentColor',
  strokeWidth: 2,
  strokeLinecap: 'round',
  strokeLinejoin: 'round',
};

const Icon = (path) => (props) => (
  <svg {...base} width={props.size || 22} height={props.size || 22} {...props}>
    {path}
  </svg>
);

export const Droplet = Icon(
  <path d="M12 2.5s6.5 7.2 6.5 12a6.5 6.5 0 1 1-13 0c0-4.8 6.5-12 6.5-12Z" />
);

export const Heart = Icon(
  <path d="M12 20.5s-7.5-4.6-10-9.3C.5 7.8 2.3 4.5 5.7 4c2-.3 3.9.6 5 2.2C11.8 4.6 13.7 3.7 15.7 4c3.4.5 5.2 3.8 3.7 7.2-2.5 4.7-10 9.3-10 9.3Z" />
);

export const Search = Icon(
  <>
    <circle cx="11" cy="11" r="7" />
    <path d="m20 20-3.5-3.5" />
  </>
);

export const Bell = Icon(
  <>
    <path d="M6 9a6 6 0 1 1 12 0c0 4.5 1.5 6 1.5 6h-15S6 13.5 6 9Z" />
    <path d="M10 20a2 2 0 0 0 4 0" />
  </>
);

export const ClipboardList = Icon(
  <>
    <rect x="5" y="4" width="14" height="17" rx="2" />
    <path d="M9 3.5h6v2H9z" />
    <path d="M8.5 11h7M8.5 14.5h7M8.5 17.5h4" />
  </>
);

export const Shield = Icon(
  <path d="M12 3 4.5 5.8V11c0 5 3.2 8.5 7.5 10 4.3-1.5 7.5-5 7.5-10V5.8L12 3Z" />
);

export const LogOut = Icon(
  <>
    <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
    <path d="m16 17 5-5-5-5" />
    <path d="M21 12H9" />
  </>
);

export const MapPin = Icon(
  <>
    <path d="M12 21s7-6.1 7-11.5a7 7 0 1 0-14 0C5 14.9 12 21 12 21Z" />
    <circle cx="12" cy="9.5" r="2.3" />
  </>
);

export const Clock = Icon(
  <>
    <circle cx="12" cy="12" r="9" />
    <path d="M12 7v5l3.2 2" />
  </>
);

export const CheckCircle = Icon(
  <>
    <circle cx="12" cy="12" r="9" />
    <path d="m8.3 12.3 2.4 2.4 5-5.2" />
  </>
);

export const XCircle = Icon(
  <>
    <circle cx="12" cy="12" r="9" />
    <path d="m9 9 6 6M15 9l-6 6" />
  </>
);

export const ArrowLeft = Icon(<path d="M19 12H5M11 5l-7 7 7 7" />);

export const Home = Icon(
  <>
    <path d="m4 11 8-7 8 7" />
    <path d="M6 10v9a1 1 0 0 0 1 1h3v-6h4v6h3a1 1 0 0 0 1-1v-9" />
  </>
);

export const Map = Icon(
  <>
    <path d="M9 4 3.5 6v14L9 18l6 2 5.5-2V4L15 6 9 4Z" />
    <path d="M9 4v14M15 6v14" />
  </>
);

export const User = Icon(
  <>
    <circle cx="12" cy="8" r="3.6" />
    <path d="M4.5 20.2a7.5 7.5 0 0 1 15 0" />
  </>
);

export const AlertTriangle = Icon(
  <>
    <path d="M10.6 3.9 2.2 18.5A1.5 1.5 0 0 0 3.5 21h17a1.5 1.5 0 0 0 1.3-2.5L13.4 3.9a1.5 1.5 0 0 0-2.8 0Z" />
    <path d="M12 9.5v4.2" />
    <circle cx="12" cy="17" r="0.15" fill="currentColor" stroke="none" />
  </>
);

export const ChevronRight = Icon(<path d="m9 6 6 6-6 6" />);

export const Users = Icon(
  <>
    <circle cx="9" cy="8" r="3.2" />
    <path d="M3.5 20a5.5 5.5 0 0 1 11 0" />
    <path d="M16.2 5.2a3.2 3.2 0 0 1 0 6.2" />
    <path d="M15.5 14.3c2.8.4 4.8 2.6 5 5.7" />
  </>
);

export const Calendar = Icon(
  <>
    <rect x="4" y="5.5" width="16" height="15" rx="2" />
    <path d="M8 3.5v4M16 3.5v4M4 10h16" />
  </>
);

export const Phone = Icon(
  <path d="M6.5 3.5h3l1.5 4-2 1.5a12 12 0 0 0 5 5l1.5-2 4 1.5v3a1.5 1.5 0 0 1-1.6 1.5A16.5 16.5 0 0 1 5 6.1 1.5 1.5 0 0 1 6.5 3.5Z" />
);

export const Scale = Icon(
  <>
    <path d="M12 3v18M6 7h12" />
    <path d="m4 7 2.5 6a2.5 2.5 0 0 0 5 0L9 7Z" />
    <path d="m15 7 2.5 6a2.5 2.5 0 0 0 5 0L20 7Z" />
  </>
);

export const CreditCard = Icon(
  <>
    <rect x="2.5" y="5.5" width="19" height="13" rx="2.2" />
    <path d="M2.5 10h19" />
    <path d="M6 14.5h4" />
  </>
);

export const Target = Icon(
  <>
    <circle cx="12" cy="12" r="8.5" />
    <circle cx="12" cy="12" r="4.8" />
    <circle cx="12" cy="12" r="1" fill="currentColor" stroke="none" />
  </>
);

export const FilePlus = Icon(
  <>
    <path d="M8 3.5h6.5L19 8v11a1.5 1.5 0 0 1-1.5 1.5h-9A1.5 1.5 0 0 1 7 19V5A1.5 1.5 0 0 1 8 3.5Z" />
    <path d="M14 3.5V8h4.5" />
    <path d="M12 12v5M9.5 14.5h5" />
  </>
);

export const RefreshCw = Icon(
  <>
    <path d="M20 11a8 8 0 0 0-14.6-4.4M4 13a8 8 0 0 0 14.6 4.4" />
    <path d="M20 4v5h-5M4 20v-5h5" />
  </>
);
