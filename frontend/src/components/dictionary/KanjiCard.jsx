export default function KanjiCard({
  character,
  strokeCount,
  jlptLevel,
  hanViet,
  onReadings,
  kunReadings,
  meaning,
}) {
  return (
    <div className="p-space-md rounded-xl border border-surface-container-high bg-surface-bright flex flex-col space-y-space-xs">
      <div className="flex items-center justify-between">
        {/* Stroke preview box */}
        <div className="w-16 h-16 rounded-lg bg-surface-container-lowest border border-outline-variant relative kanji-grid-bg flex items-center justify-center">
          <span className="font-kanji-headline text-3xl text-primary font-bold">{character}</span>
        </div>
        <div className="text-right">
          <span className="text-label-sm font-label-sm bg-surface-container px-2 py-0.5 rounded text-on-surface font-bold">
            {strokeCount} nét
          </span>
          <p className="text-label-sm font-label-sm text-outline mt-1">{jlptLevel}</p>
        </div>
      </div>
      <div className="mt-2">
        <div className="font-headline-sm font-headline-sm text-on-surface">{hanViet}</div>
        <p className="text-body-sm font-body-sm text-on-surface-variant">
          On: {onReadings} • Kun: {kunReadings}
        </p>
        <p className="text-label-sm font-label-sm text-secondary mt-1">Nghĩa: {meaning}</p>
      </div>
    </div>
  );
}
