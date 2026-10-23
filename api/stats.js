const { db, calculateReports, setCors } = require('./data');

module.exports = (req, res) => {
  setCors(res);
  if (req.method === 'OPTIONS') {
    return res.status(204).end();
  }

  const reports = calculateReports('ALL');
  let totalPresent = 0;
  let totalClasses = 0;
  let defaulters = 0;

  reports.forEach(r => {
    totalPresent += (r.presentCount + r.lateCount);
    totalClasses += r.totalClasses;
    if (r.totalClasses > 0 && r.percentage < 75.0) {
      defaulters++;
    }
  });

  const avgAttendance = totalClasses > 0 ? Number(((totalPresent / totalClasses) * 100).toFixed(1)) : 0.0;

  res.status(200).json({
    totalStudents: db.students.length,
    totalSubjects: db.subjects.length,
    totalClassesConducted: totalClasses,
    averageAttendance: avgAttendance,
    defaultersCount: defaulters,
    dbType: 'Vercel Cloud Serverless'
  });
};
