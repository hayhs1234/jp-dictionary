import SearchBar from '../components/dictionary/SearchBar.jsx';
import WordCard from '../components/dictionary/WordCard.jsx';
import TranslatorWidget from '../components/dictionary/TranslatorWidget.jsx';
import SidebarWidgets from '../components/dictionary/SidebarWidgets.jsx';

export default function HomePage() {
  return (
    <>
      {/* 1. HERO SEARCH & INPUT CONTROL SECTION */}
      <SearchBar />

      {/* 2 & 3. MAIN CONTENT: 12-COLUMN ASYMMETRIC BENTO GRID */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-space-lg items-start">
        {/* LEFT & CENTER VIEW (8 COLS): RICH DETAILED DEFINITION */}
        <section className="lg:col-span-8 flex flex-col space-y-space-lg">
          <WordCard />
        </section>

        {/* RIGHT COLUMN (4 COLS): DUAL TRANSLATOR & SEARCH RADAR */}
        <aside className="lg:col-span-4 flex flex-col space-y-space-lg">
          <TranslatorWidget />
          <SidebarWidgets />
        </aside>
      </div>
    </>
  );
}
