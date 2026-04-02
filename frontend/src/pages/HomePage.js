import React, { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import {
  Zap,
  Wrench,
  Snowflake,
  Hammer,
  Tv,
  Search,
  CalendarCheck,
  CheckCircle2,
  MapPin,
  ShieldCheck,
  Clock3,
  MessageCircle,
  CreditCard,
  Navigation,
  Star,
  AlertTriangle,
  Users,
  Sparkles,
  Check,
} from "lucide-react";
import { api } from "../utils/api";

const categories = [
  { title: "Electrician", icon: Zap },
  { title: "Plumber", icon: Wrench },
  { title: "AC Repair", icon: Snowflake },
  { title: "Carpenter", icon: Hammer },
  { title: "Appliance Repair", icon: Tv },
];

const steps = [
  { title: "Search service", icon: Search },
  { title: "Book provider", icon: CalendarCheck },
  { title: "Get work done", icon: CheckCircle2 },
];

const features = [
  { title: "Location-based search", icon: MapPin },
  { title: "Verified providers", icon: ShieldCheck },
  { title: "Real-time booking", icon: Clock3 },
  { title: "Chat with providers", icon: MessageCircle },
  { title: "Secure online payments", icon: CreditCard },
  { title: "Live route guidance", icon: Navigation },
  { title: "Ratings and reviews", icon: Star },
  { title: "Issue reporting support", icon: AlertTriangle },
];

const fallbackStats = [
  { value: "10K+", label: "Service requests completed" },
  { value: "2K+", label: "Verified providers" },
  { value: "25+", label: "Neighborhoods covered" },
  { value: "4.8/5", label: "Average user rating" },
];

const fallbackTopProviders = [
  {
    name: "Arun Kumar",
    service: "Electrical Specialist",
    rating: "4.9",
    jobs: "320 jobs",
  },
  {
    name: "Prakash S",
    service: "AC Repair Expert",
    rating: "4.8",
    jobs: "280 jobs",
  },
  {
    name: "Suresh M",
    service: "Carpentry Professional",
    rating: "4.9",
    jobs: "300 jobs",
  },
];

const formatCompact = (value) => {
  if (value >= 1000) return `${Math.round(value / 100) / 10}K+`;
  return `${Math.max(0, Math.round(value))}`;
};

const getNeighborhood = (location = "") => {
  if (!location || typeof location !== "string") return "";
  const parts = location
    .split(",")
    .map((part) => part.trim())
    .filter(Boolean);

  if (parts.length >= 2) return parts[parts.length - 2].toLowerCase();
  return parts[0]?.toLowerCase() || "";
};

export const HomePage = () => {
  const [servicesData, setServicesData] = useState([]);
  const [isUsingFallback, setIsUsingFallback] = useState(true);

  useEffect(() => {
    let isActive = true;

    const loadHomeData = async () => {
      try {
        const response = await api.get("/services");
        const rows = Array.isArray(response.data) ? response.data : [];

        if (!isActive) return;
        if (rows.length > 0) {
          setServicesData(rows);
          setIsUsingFallback(false);
          return;
        }

        setIsUsingFallback(true);
      } catch {
        if (!isActive) return;
        setIsUsingFallback(true);
      }
    };

    loadHomeData();

    return () => {
      isActive = false;
    };
  }, []);

  const dynamicStats = useMemo(() => {
    if (isUsingFallback || servicesData.length === 0) return fallbackStats;

    const uniqueProviders = new Set();
    const uniqueNeighborhoods = new Set();

    let completedJobs = 0;
    let weightedRatingSum = 0;
    let totalReviews = 0;

    servicesData.forEach((service) => {
      if (service?.providerId != null) uniqueProviders.add(service.providerId);

      const neighborhood = getNeighborhood(service?.providerLocation || "");
      if (neighborhood) uniqueNeighborhoods.add(neighborhood);

      completedJobs += Number(service?.completedJobs || 0);

      const rating = Number(service?.rating || 0);
      const reviews = Number(service?.reviews || 0);
      weightedRatingSum += rating * reviews;
      totalReviews += reviews;
    });

    const avgRating = totalReviews > 0 ? weightedRatingSum / totalReviews : 4.8;

    return [
      {
        value: formatCompact(completedJobs || servicesData.length * 3),
        label: "Service requests completed",
      },
      {
        value: formatCompact(uniqueProviders.size),
        label: "Verified providers",
      },
      {
        value: `${Math.max(1, uniqueNeighborhoods.size)}+`,
        label: "Neighborhoods covered",
      },
      {
        value: `${avgRating.toFixed(1)}/5`,
        label: "Average user rating",
      },
    ];
  }, [isUsingFallback, servicesData]);

  const dynamicTopProviders = useMemo(() => {
    if (isUsingFallback || servicesData.length === 0) return fallbackTopProviders;

    const byProvider = new Map();

    servicesData.forEach((service) => {
      const providerId = service?.providerId;
      if (providerId == null) return;

      const existing = byProvider.get(providerId) || {
        name: service?.providerName || "Provider",
        service: service?.category ? `${service.category} Specialist` : "Service Professional",
        ratingSum: 0,
        reviewCount: 0,
        completedJobs: 0,
      };

      const rating = Number(service?.rating || 0);
      const reviews = Number(service?.reviews || 0);
      existing.ratingSum += rating * reviews;
      existing.reviewCount += reviews;
      existing.completedJobs = Math.max(existing.completedJobs, Number(service?.completedJobs || 0));

      byProvider.set(providerId, existing);
    });

    const providers = Array.from(byProvider.values())
      .map((provider) => {
        const avgRating =
          provider.reviewCount > 0 ? provider.ratingSum / provider.reviewCount : 4.7;
        return {
          name: provider.name,
          service: provider.service,
          rating: avgRating.toFixed(1),
          jobs: `${provider.completedJobs || 1} jobs`,
          score: avgRating * 1000 + provider.completedJobs,
        };
      })
      .sort((a, b) => b.score - a.score)
      .slice(0, 3)
      .map(({ score, ...provider }) => provider);

    return providers.length > 0 ? providers : fallbackTopProviders;
  }, [isUsingFallback, servicesData]);

  return (
    <div className="min-h-screen bg-dark-950 text-dark-100 overflow-x-hidden">
      <header className="sticky top-0 z-30 border-b border-dark-700/70 bg-dark-950/90 backdrop-blur">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
          <Link to="/" className="font-display font-bold text-2xl text-white">
            FixItNow
          </Link>
          <div className="flex items-center gap-3">
            <Link to="/login" className="btn-secondary py-2.5 px-5 text-sm">
              Login
            </Link>
            <Link to="/register" className="btn-primary py-2.5 px-5 text-sm">
              Register
            </Link>
          </div>
        </div>
      </header>

      <main>
        <section className="relative overflow-hidden border-b border-dark-800 bg-gradient-to-br from-dark-950 via-dark-900 to-dark-950">
          <div className="absolute -top-24 -left-16 w-96 h-96 rounded-full bg-brand-500/20 blur-3xl" />
          <div className="absolute -bottom-24 -right-16 w-96 h-96 rounded-full bg-blue-500/10 blur-3xl" />
          <div className="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-20 md:py-28">
            <div className="max-w-4xl mx-auto text-center">
              <div className="inline-flex items-center gap-2 text-xs font-medium tracking-wide px-3 py-1.5 rounded-full border border-brand-500/40 bg-brand-500/10 text-brand-300 mb-5">
                <Sparkles className="w-3.5 h-3.5" />
                Trusted Local Services Platform
            </div>
              <h1 className="font-display font-bold text-3xl sm:text-4xl md:text-5xl text-white leading-tight">
                Find Trusted Service Providers Near You
              </h1>
              <p className="mt-4 text-dark-300 text-base sm:text-lg max-w-2xl mx-auto">
                Book electricians, plumbers, AC repair and more instantly
              </p>
              <div className="mt-8 flex flex-col sm:flex-row items-center justify-center gap-3">
                <Link to="/customer/services" className="btn-primary w-full sm:w-auto">
                  Find Services
                </Link>
                <Link to="/register" className="btn-secondary w-full sm:w-auto">
                  Become a Provider
                </Link>
              </div>
              <div className="mt-8 flex flex-wrap items-center justify-center gap-3 text-sm text-dark-300">
                <span className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full bg-dark-800/70 border border-dark-700">
                  <Check className="w-3.5 h-3.5 text-green-400" /> Verified pros
                </span>
                <span className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full bg-dark-800/70 border border-dark-700">
                  <Check className="w-3.5 h-3.5 text-green-400" /> Real-time status
                </span>
                <span className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full bg-dark-800/70 border border-dark-700">
                  <Check className="w-3.5 h-3.5 text-green-400" /> In-app chat
                </span>
              </div>
              {isUsingFallback && (
                <p className="mt-4 text-xs text-dark-400">
                  Showing preview metrics. Live numbers appear when service data is available.
                </p>
              )}
            </div>
          </div>
        </section>

        <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10 md:py-12">
          <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
            {dynamicStats.map((item) => (
              <div
                key={item.label}
                className="bg-dark-800 border border-dark-700 rounded-2xl p-5 text-center transition-all duration-300 hover:-translate-y-1 hover:shadow-[0_16px_30px_rgba(0,0,0,0.35)] hover:border-brand-500/40"
              >
                <p className="font-display text-2xl md:text-3xl font-bold text-brand-400">{item.value}</p>
                <p className="text-sm text-dark-300 mt-1">{item.label}</p>
              </div>
            ))}
          </div>
        </section>

        <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-14 md:py-16">
          <h2 className="font-display font-semibold text-2xl text-white mb-6">Service Categories</h2>
          <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-4">
            {categories.map((item) => {
              const Icon = item.icon;
              return (
                <div
                  key={item.title}
                  className="bg-dark-800 border border-dark-700 rounded-2xl p-5 flex flex-col items-center text-center gap-3 transition-all duration-300 hover:scale-[1.03] hover:shadow-[0_16px_30px_rgba(0,0,0,0.35)] hover:border-brand-500/40"
                >
                  <div className="w-11 h-11 rounded-xl bg-dark-700 flex items-center justify-center transition-colors group-hover:bg-dark-600">
                    <Icon className="w-5 h-5 text-brand-400" />
                  </div>
                  <p className="text-sm sm:text-base font-medium text-dark-100">{item.title}</p>
                </div>
              );
            })}
          </div>
        </section>

        <section className="border-y border-dark-800 bg-dark-900/40">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-14 md:py-16">
            <div className="flex items-center justify-between flex-wrap gap-3 mb-6">
              <h2 className="font-display font-semibold text-2xl text-white">Top Providers</h2>
              <p className="text-dark-400 text-sm">Trusted professionals with strong ratings and completed jobs</p>
            </div>
            <div className="grid md:grid-cols-3 gap-4">
              {dynamicTopProviders.map((provider) => (
                <div
                  key={provider.name}
                  className="bg-dark-800 border border-dark-700 rounded-2xl p-5 transition-all duration-300 hover:-translate-y-1 hover:shadow-[0_16px_30px_rgba(0,0,0,0.35)] hover:border-brand-500/40"
                >
                  <div className="flex items-start justify-between gap-3">
                    <div>
                      <p className="font-semibold text-white">{provider.name}</p>
                      <p className="text-sm text-dark-400 mt-0.5">{provider.service}</p>
                    </div>
                    <span className="inline-flex items-center gap-1 text-xs px-2.5 py-1 rounded-full bg-green-500/20 border border-green-500/30 text-green-300">
                      <ShieldCheck className="w-3.5 h-3.5" /> Verified
                    </span>
                  </div>
                  <div className="mt-4 flex items-center justify-between text-sm">
                    <p className="text-dark-300 inline-flex items-center gap-1.5">
                      <Star className="w-4 h-4 text-yellow-400" /> {provider.rating}
                    </p>
                    <p className="text-dark-400 inline-flex items-center gap-1.5">
                      <Users className="w-4 h-4" /> {provider.jobs}
                    </p>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </section>

        <section className="border-y border-dark-800 bg-dark-900/40">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-14 md:py-16">
            <h2 className="font-display font-semibold text-2xl text-white mb-6">How It Works</h2>
            <div className="grid md:grid-cols-3 gap-4">
              {steps.map((item, index) => {
                const Icon = item.icon;
                return (
                  <div
                    key={item.title}
                    className="bg-dark-800 border border-dark-700 rounded-2xl p-5 transition-all duration-300 hover:scale-[1.02] hover:shadow-[0_16px_30px_rgba(0,0,0,0.35)] hover:border-brand-500/40"
                  >
                    <div className="w-10 h-10 rounded-xl bg-brand-500/20 border border-brand-500/30 text-brand-400 flex items-center justify-center mb-3">
                      <Icon className="w-5 h-5" />
                    </div>
                    <p className="text-xs text-dark-400">Step {index + 1}</p>
                    <p className="font-medium text-white mt-1">{item.title}</p>
                  </div>
                );
              })}
            </div>
          </div>
        </section>

        <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-14 md:py-16">
          <h2 className="font-display font-semibold text-2xl text-white mb-2">Platform Features</h2>
          <p className="text-dark-400 mb-6">Everything available inside FixItNow for customers and providers.</p>
          <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-4">
            {features.map((item) => {
              const Icon = item.icon;
              return (
                <div
                  key={item.title}
                  className="bg-dark-800 border border-dark-700 rounded-2xl p-5 transition-all duration-300 hover:-translate-y-1 hover:shadow-[0_16px_30px_rgba(0,0,0,0.35)] hover:border-brand-500/40"
                >
                  <Icon className="w-5 h-5 text-brand-400 mb-3" />
                  <p className="font-medium text-white">{item.title}</p>
                </div>
              );
            })}
          </div>
        </section>

        <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pb-16">
          <div className="bg-gradient-to-r from-dark-800 to-dark-900 border border-dark-700 rounded-2xl p-8 md:p-10 text-center shadow-[0_20px_40px_rgba(0,0,0,0.35)]">
            <h2 className="font-display font-bold text-2xl md:text-3xl text-white">Get Started Now</h2>
            <p className="text-dark-300 mt-3 max-w-xl mx-auto">
              Join FixItNow to discover trusted local professionals, manage bookings, and get your work done without delays.
            </p>
            <div className="mt-6 flex flex-col sm:flex-row items-center justify-center gap-3">
              <Link to="/login" className="btn-secondary w-full sm:w-auto">
                Login
              </Link>
              <Link to="/register" className="btn-primary w-full sm:w-auto">
                Register
              </Link>
            </div>
          </div>
        </section>
      </main>

      <footer className="border-t border-dark-800">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 text-sm text-dark-400">
          <p className="text-dark-200 font-medium">FixItNow</p>
          <p className="mt-1">Neighborhood service marketplace for fast, trusted home services.</p>
        </div>
      </footer>
    </div>
  );
};
